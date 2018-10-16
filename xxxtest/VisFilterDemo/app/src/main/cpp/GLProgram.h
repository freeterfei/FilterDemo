//
// Created by 陈飞飞 on 2018/9/17.
//

#ifndef VISFILTERDEMO_GLPROGRAM_H
#define VISFILTERDEMO_GLPROGRAM_H

#include "GLCommon.h"
#include <string>


class GLProgram
{
public:
    GLProgram();
    virtual ~GLProgram();

public:
    bool initProgram(const std::string& vertexShader, const std::string& fragmentShader);
    void useProgram();

    GLuint getUniformLocation(std::string uniformName);
    GLuint getAttribLocation(std::string attributeName);

private:
    bool loadShader(GLuint *shader, GLenum type, std::string shaderString);

private:
    GLuint mProgram;
};

#endif //VISFILTERDEMO_GLPROGRAM_H
