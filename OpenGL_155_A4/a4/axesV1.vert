#version 430


layout (location = 1) in vec2 tex_coord;




uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout (binding=0) uniform sampler2D s;

out vec4 varyingColor;

const vec4 vertices[6] = vec4[6]
(
vec4(0.0,0.0,0.0, 1.0),
vec4( 10.0,0.0,0.0, 1.0),
vec4(0.0,0.0,0.0, 1.0),
vec4( 0.0,10.0,0.0, 1.0),
vec4(0.0,0.0,0.0, 1.0),
vec4( 0.0,0.0,10.0, 1.0));



void main(void)
{	
        if(gl_VertexID == 0){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(1.0f,0.0f,0.0f,1.0f);
        }
        if(gl_VertexID == 1){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(1.0f,0.0f,0.0f,1.0f);
        } 
        if(gl_VertexID == 2){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(0.0f,1.0f,0.0f,1.0f);
        }
        if(gl_VertexID == 3){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(0.0f,1.0f,0.0f,1.0f);
        }
        if(gl_VertexID == 4){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(0.0f,0.0f,1.0f,1.0f);
        }
        if(gl_VertexID == 5){
            
            gl_Position = proj_matrix * mv_matrix * vertices[gl_VertexID];
            varyingColor = vec4(0.0f,0.0f,1.0f,1.0f);
        }
        
       
        
}

