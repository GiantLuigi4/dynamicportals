package tfc.dynamicportals.mixin.client.core;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.dynamicportals.GLUtils;
import tfc.dynamicportals.ShaderInjections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// I'm not even gonna attempt to explain what's happening here
@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
	@Unique
	private static HashMap<Integer, Integer> shaderToTypeMap = new HashMap<>();
	
	@Inject(at = @At("RETURN"), method = "glCreateShader")
	private static void postCreateShader(int pType, CallbackInfoReturnable<Integer> cir) {
		shaderToTypeMap.put(cir.getReturnValue(), pType);
	}
	
	@Inject(at = @At("HEAD"), method = "glDeleteShader")
	private static void preDeleteShader(int pShader, CallbackInfo ci) {
		shaderToTypeMap.remove(pShader);
	}
	
	@Inject(at = @At("HEAD"), method = "glShaderSource")
	private static void preGlShaderSource(int s, List<String> pointerbuffer, CallbackInfo ci) {
		int type = shaderToTypeMap.get(s);
		
		StringBuilder str = new StringBuilder();
		for (String s1 : pointerbuffer) {
			str.append(s1).append("\n");
		}
		String[] list = str.toString().split("\n");
		AtomicInteger lCC = new AtomicInteger();
		boolean inMain = false;
		boolean hitUniforms = false;
		boolean hitOuts = false;
		boolean hitInputs = false;
		StringBuilder output = new StringBuilder();
		
		boolean hasTexCoordInput = false;
		String samplerName = null;
		
		for (String s1 : list) {
			String srcStr = s1;
			int len = s1.length();
			s1 = s1.replace("  ", " ");
			while (len != s1.length()) {
				s1 = s1.replace("  ", " ");
				s1 = s1.replace("( ", " ");
				s1 = s1.replace(" (", " ");
				s1 = s1.replace(" )", " ");
				s1 = s1.replace(") ", " ");
				s1 = s1.replace("\t", "");
				len = s1.length();
			}
			// TODO: make this stuff more reliable
			if (s1.startsWith("out vec4 fragColor")) hasTexCoordInput = true;
			if (s1.startsWith("uniform sampler2D")) {
				String str1 = s1.replace("uniform sampler2D ", "").trim();
				if (str1.startsWith("Sampler0") || str1.startsWith("DiffuseSampler"))
					samplerName = str1.substring(0, str1.length() - 1);
			}
			if (!hitUniforms && s1.trim().startsWith("uniform")) {
				srcStr = injectUniforms(type, srcStr);
				hitUniforms = true;
			}
			if (!hitOuts && s1.trim().startsWith("out")) {
				srcStr = injectOuts(type, srcStr);
				hitOuts = true;
			}
			if (!hitInputs && s1.trim().startsWith("in")) {
				srcStr = injectIns(type, srcStr);
				hitInputs = true;
			}
			if (hitUniforms && hitOuts && hitInputs) {
				if (inMain) {
					srcStr = checkLineAndInject(type, srcStr, lCC, hasTexCoordInput && samplerName != null, samplerName);
					if (lCC.get() == 0) inMain = false;
				}
				if (s1.contains("void main()")) {
					inMain = true;
					srcStr = checkLineAndInject(type, srcStr, lCC, hasTexCoordInput && samplerName != null, samplerName);
				}
			}
			output.append(srcStr).append("\n");
		}
		System.out.println(output.toString());
		pointerbuffer.clear();
		for (String s1 : output.toString().split("\n")) {
			pointerbuffer.add(s1 + "\n");
		}
	}
	
	private static String injectUniforms(int type, String srcStr) {
		if (type == GL42.GL_FRAGMENT_SHADER) {
			String str =
					"/* Dynamic Portals injection */\n" +
							"uniform int dynamicPortalsHasStencilTextureSet;\n" +
							"uniform sampler2D dynamicPortalsStencilTexture;\n" +
							"uniform sampler2D dynamicPortalsStencilDepth;\n" +
							"uniform vec2 dynamicPortalsFBOSize;\n" +
							"/* end Dynamic Portals injection */\n";
			srcStr = str + srcStr;
		}
		return srcStr;
	}
	
	private static String injectOuts(int type, String srcStr) {
		return srcStr;
	}
	
	private static String injectIns(int type, String srcStr) {
		return srcStr;
	}
	
	private static String checkLineAndInject(int type, String line, AtomicInteger lCC, boolean hasTexCoordInput, String samplerName) {
		String[] split = split(line, "{}");
		StringBuilder builder = new StringBuilder();
		for (String str : split) {
			if (str.contains("{")) {
				builder.append(str);
				if (lCC.get() == 0) {
					if (type == GL42.GL_FRAGMENT_SHADER) {
						String injection = ShaderInjections.headInjection(hasTexCoordInput, samplerName);
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
	
	@Inject(at = @At("HEAD"), method = "_enableCull")
	private static void preEnableCull(CallbackInfo ci) {
		if (GLUtils.shouldSwapBackface())
			GL11.glCullFace(GL11.GL_BACK);
	}
}
