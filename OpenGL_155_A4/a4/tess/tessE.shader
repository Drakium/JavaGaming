#version 430

layout (quads, equal_spacing,ccw) in;

uniform mat4 mvp;
layout (binding = 0) uniform sampler2D tex_color;

out vec2 tes_out;

void main (void)
{	vec2 tc = vec2(gl_TessCoord.x, 1.0-gl_TessCoord.y);

	vec4 tessellatedPoint = vec4(gl_TessCoord.x-0.5, 0.0, gl_TessCoord.y-0.5, 1.0);
	
	// add the height from the height map to the vertex:
	tessellatedPoint.y += (texture(tex_color, tc).r) / 20.0;
	
	gl_Position = mvp * tessellatedPoint;
	tes_out = tc;
}