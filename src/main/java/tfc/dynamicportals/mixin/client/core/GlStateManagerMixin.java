package tfc.dynamicportals.mixin.client.core;

import com.mojang.blaze3d.platform.GlStateManager;
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
public class GlStateManagerMixin {
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
		
		String shaderFile = String.join("\n", pointerBuffer);
		if (shaderFile.contains("#dynportals skip_inject")) {
			pointerBuffer.clear();
			Arrays.stream(shaderFile.replace("#dynportals skip_inject", "").split("\n")).forEach((str) -> pointerBuffer.add(str + "\n"));
		}
		
		AtomicInteger lCC = new AtomicInteger();
		boolean inMain = false;
		boolean hitUniforms = false;
		boolean hitOuts = false;
		boolean hitInputs = false;
		boolean hasTexCoordInput = false;
		boolean hasColorInput = false;
		String samplerName = null;
		
		StringBuilder output = new StringBuilder();
		for (String line : shaderFile.split("\n")) {
			String injected = line;
			int len = line.length();
			line = line.replace("  ", " ");
			while (len != line.length()) {
				len = line.length();
				line = line
						.replace("  ", " ")
						.replace("( ", " ")
						.replace(" (", " ")
						.replace(" )", " ")
						.replace(") ", " ")
						.replace("\t", "")
						.trim();
			}
			// TODO: make this stuff more reliable
			// Well actually, I don't know what you mean by "reliable", I tried to make it "optimized"
			// I'll leave this as "myTODO" but I won't touch it for a bit
			hasTexCoordInput = hasTexCoordInput || line.startsWith("out vec4 fragColor");
			hasColorInput = hasColorInput || line.startsWith("in vec4 vertexColor");

			if (line.startsWith("uniform sampler2D")) {
				String str1 = line.replace("uniform sampler2D ", "").trim();
				if (str1.startsWith("Sampler0") || str1.startsWith("DiffuseSampler"))
					samplerName = str1.substring(0, str1.length() - 1);
			}
			if (!hitUniforms && line/*.trim()*/.startsWith("uniform")) {
				injected = injectUniforms(type, injected);
				hitUniforms = true;
			}
			if (!hitOuts && line/*.trim()*/.startsWith("out")) {
				injected = injectOuts(type, injected);
				hitOuts = true;
			}
			if (!hitInputs && line/*.trim()*/.startsWith("in")) {
				injected = injectIns(type, injected);
				hitInputs = true;
			}
			if (hitUniforms && hitOuts && hitInputs && (inMain || line.contains("void main()"))) {
				injected = checkLineAndInject(type, injected, lCC, hasTexCoordInput && samplerName != null, samplerName, hasColorInput);
				inMain = (!inMain || lCC.get() != 0) && (line.contains("void main()") || inMain);
//				if (inMain) {
//					injected = checkLineAndInject(type, injected, lCC, hasTexCoordInput && samplerName != null, samplerName, hasColorInput);
//					if (lCC.get() == 0) inMain = false;
//				}
//				if (line.contains("void main()")) {
//					injected = checkLineAndInject(type, injected, lCC, hasTexCoordInput && samplerName != null, samplerName, hasColorInput);
//					inMain = true;
//				}
			}
			output.append(injected).append("\n");
		}
//		System.out.println(output);
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
	}
	
	private static String injectUniforms(int type, String srcStr) {
		String str = "";
		if (type == GL42.GL_FRAGMENT_SHADER) {
			str =
					"""
					/* Dynamic Portals injection */
					uniform int dynamicPortalsHasStencilTextureSet;
					uniform sampler2D dynamicPortalsStencilTexture;
					uniform sampler2D dynamicPortalsStencilDepth;
					uniform vec2 dynamicPortalsFBOSize;
					/* end Dynamic Portals injection */
					""";
		} else if (type == GL42.GL_VERTEX_SHADER) {
			str =
					"""
					/* Dynamic Portals injection */
					uniform int dynamicPortalsHasStencilTextureSet;
					/* end Dynamic Portals injection */
					""";
		}
		return srcStr + str;
	}
	
	private static String injectOuts(int type, String srcStr) {
		return srcStr;
	}
	
	private static String injectIns(int type, String srcStr) {
		return srcStr;
	}
	
	private static String checkLineAndInject(int type, String line, AtomicInteger lCC, boolean hasTexCoordInput, String samplerName, boolean hasColorInput) {
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
						String injection = ShaderInjections.tailVertex();
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
			if (!delim.contains("" + c)) {
				builder.append(c);
			} else {
				if (!builder.toString().isEmpty())
					strings.add(builder.toString());
				strings.add("" + c);
				builder = new StringBuilder();
			}
		}
		if (!builder.toString().isEmpty())
			strings.add(builder.toString());
		return strings.toArray(new String[0]);
	}
}
