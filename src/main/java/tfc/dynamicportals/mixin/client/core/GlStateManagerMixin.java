package tfc.dynamicportals.mixin.client.core;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL42;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.ShaderInjections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// I'm not even gonna attempt to explain what's happening here
// lorenzo: I'll destroy this class anyway
@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {
	@Unique
	private static final HashMap<Integer, Integer> shaderToTypeMap = new HashMap<>();
	
	@Inject(at = @At("RETURN"), method = "glCreateShader")
	private static void postCreateShader(int pType, CallbackInfoReturnable<Integer> cir) {
		shaderToTypeMap.put(cir.getReturnValue(), pType);
	}
	
	@Inject(at = @At("HEAD"), method = "glDeleteShader")
	private static void preDeleteShader(int pShader, CallbackInfo ci) {
		shaderToTypeMap.remove(pShader);
	}
	
	// compatibility hack
	@ModifyVariable(at = @At("HEAD"), method = "glShaderSource", index = 1, argsOnly = true)
	private static List<String> convertToArrayList(List<String> lines) {
		return new ArrayList<>(lines);
	}
	
	// shader injections
	@Inject(at = @At("HEAD"), method = "glShaderSource")
	private static void preGlShaderSource(int s, List<String> pointerBuffer, CallbackInfo ci) {
		int type = shaderToTypeMap.get(s);
		
		String shaderFile = String.join("\n", pointerBuffer).trim().replace("\n\n", "\n");
		if (shaderFile.contains("#dynportals skip_inject")) {
			pointerBuffer.clear();
			Arrays.stream(shaderFile.replace("#dynportals skip_inject", "").split("\n")).forEach((str) -> pointerBuffer.add(str + "\n"));
		}
		
		AtomicInteger lCC = new AtomicInteger();
		boolean inMain = false;
		boolean hitUniforms = false;
		boolean hitOuts = false;
		boolean hitInputs = false;
		boolean hasTexCoordInput = shaderFile.contains("out vec4 fragColor");
		boolean hasColorInput = shaderFile.contains("in vec4 vertexColor");
		boolean hasModelViewMat = shaderFile.contains("uniform mat4 ModelViewMat");
		boolean hasProjMat = shaderFile.contains("uniform mat4 ProjMat");
		
		boolean isBlock = shaderFile.contains("uniform vec3 ChunkOffset");
		String samplerName = null;
		
		StringBuilder output = new StringBuilder();
		for (String line : shaderFile.split("\n")) {
			line = line.trim();
			String injected = line;
			if (line.startsWith("uniform sampler2D")) {
				String str1 = line.replace("uniform sampler2D ", "").trim().replace(";", "");
				if (str1.startsWith("Sampler0") || str1.startsWith("DiffuseSampler"))
					samplerName = str1;
			}
			if (!hitUniforms && line.startsWith("uniform")) {
				injected = injectUniforms(type, injected);
				hitUniforms = true;
			}
			if (!hitOuts && line.startsWith("out")) {
				injected = injectOuts(type, injected);
				hitOuts = true;
			}
			if (!hitInputs && line.startsWith("in")) {
				injected = injectIns(type, injected);
				hitInputs = true;
			}
			if (hitUniforms && hitOuts && hitInputs && (inMain || line.contains("void main()"))) {
				injected = checkLineAndInject(type, injected, lCC, hasTexCoordInput && samplerName != null, samplerName, hasColorInput, hasProjMat, hasModelViewMat, isBlock);
				inMain = (!inMain || lCC.get() != 0) && (line.contains("void main()") || inMain);
			}
			output.append(injected).append("\n");
		}
//		if (output.toString().contains("iris")) {
//			String path =
//					"shader/" +
//							((type == GL42.GL_FRAGMENT_SHADER) ? "frag" : ((type == GL42.GL_VERTEX_SHADER) ? "vert" : "geom")) +
//							s + "." +
//							((type == GL42.GL_FRAGMENT_SHADER) ? "fsh" : ((type == GL42.GL_VERTEX_SHADER) ? "vsh" : "glsl"));
//			File file = new File("shader");
//			if (!file.exists()) file.mkdirs();
//			File fl = new File(path);
//			try {
//				if (!fl.exists()) fl.createNewFile();
//				FileOutputStream outputStream = new FileOutputStream(fl);
//				outputStream.write(output.toString().getBytes());
//				outputStream.close();
//				outputStream.flush();
//			} catch (Throwable ignored) {
//				ignored.printStackTrace();
//			}
//		}
		pointerBuffer.clear();
		Arrays.stream(output.toString().split("\n")).forEach((str)->pointerBuffer.add(str + "\n"));
		System.out.println(output);
	}
	
	private static String injectUniforms(int type, String srcStr) {
		srcStr += """
				
				/* Dynamic Portals injection */
				uniform int dynamicPortalsHasStencilTextureSet;
				""";
		if (type == GL42.GL_FRAGMENT_SHADER) {
			srcStr +=
					"""
					uniform sampler2D dynamicPortalsStencilTexture;
					uniform sampler2D dynamicPortalsStencilDepth;
					uniform vec2 dynamicPortalsFBOSize;
					""";
		}
		return srcStr +
                """
				/* end Dynamic Portals injection */
				""";
	}
	
	private static String injectOuts(int type, String srcStr) {
		if (type == GL20.GL_VERTEX_SHADER)  {
			srcStr +=
					"""
     
					/* Dynamic Portals injection */
					out vec4 dynamicPortalsWorldPos;
					/* end Dynamic Portals injection */
					""";
		}
		return srcStr;
	}
	
	private static String injectIns(int type, String srcStr) {
		if (type == GL20.GL_FRAGMENT_SHADER)  {
			srcStr +=
					"""
     
					/* Dynamic Portals injection */
					in vec4 dynamicPortalsWorldPos;
					/* end Dynamic Portals injection */
					""";
		}
		return srcStr;
	}
	
	private static String checkLineAndInject(int type, String line, AtomicInteger lCC, boolean hasTexCoordInput, String samplerName, boolean hasColorInput, boolean hasProjMat, boolean hasModelViewMat, boolean isBlock) {
		String[] split = split(line, "{}");
		StringBuilder builder = new StringBuilder();
		for (String str : split) {
			if (str.contains("{")) {
				builder.append(str);
				if (lCC.get() == 0) {
					if (type == GL42.GL_FRAGMENT_SHADER) {
						String injection = ShaderInjections.headInjection(hasTexCoordInput, samplerName, hasColorInput);
						builder.append(injection);
					}
				}
				lCC.incrementAndGet();
			} else if (str.contains("}")) {
				lCC.decrementAndGet();
				if (lCC.get() == 0) {
					if (type == GL42.GL_FRAGMENT_SHADER) {
						String injection = ShaderInjections.tailInjection();
						builder.append(injection);
					} else if (type == GL42.GL_VERTEX_SHADER) {
						String injection = ShaderInjections.tailVertex(hasModelViewMat, hasProjMat, isBlock);
						builder.append(injection);
					}
				}
				builder.append(str);
			} else {
				builder.append(str);
			}
		}
		return builder.toString();
	}
	
	@Unique
	private static String[] split(String str, String delim) {
		ArrayList<String> strings = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (!delim.contains(String.valueOf(c))) {
				builder.append(c);
			} else {
				if (!builder.toString().isEmpty())
					strings.add(builder.toString());
				strings.add(String.valueOf(c));
				builder = new StringBuilder();
			}
		}
		if (!builder.toString().isEmpty())
			strings.add(builder.toString());
		return strings.toArray(new String[0]);
	}
}
