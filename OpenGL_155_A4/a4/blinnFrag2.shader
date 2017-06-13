#version 430

in vec3 vNormal, vLightDir, vVertPos, vHalfVec;


in vec4 shadow_coord;
out vec4 fragColor;
in vec2 tc;
 
struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D s;

void main(void)
{	vec3 L = normalize(vLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(vHalfVec);
	
	float inShadow = textureProj(shadowTex, shadow_coord);
	
	vec3 ambient =((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
        vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(dot(L,N),0.0);
        vec3 specular = material.specular.xyz * light.specular.xyz * pow(max(dot(H,N), 0.0f),material.shininess);

        
        
        fragColor = .8 * (texture(s,tc)) + (0.4 * vec4((ambient + diffuse + specular), 1.0));                      
	if (inShadow != 0.0)
	{	fragColor += light.diffuse * material.diffuse * max(dot(L,N),0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0);
	}
        
}
