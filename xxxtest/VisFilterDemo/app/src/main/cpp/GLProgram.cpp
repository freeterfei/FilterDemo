//
// Created by 陈飞飞 on 2018/9/17.
//

#include "GLProgram.h"


GLProgram::GLProgram()
{
    mProgram = -1;
}

GLProgram::~GLProgram()
{
    if (mProgram) {
        glDeleteProgram(mProgram);
        mProgram = -1;
    }
}

bool GLProgram::initProgram(const std::string &vertexString, const std::string &fragmentString)
{
    if(mProgram) {
        LOGI("program is already initialized");
        return true;
    }

    GLuint vertexShader, fragmentShader;
    // Create and compile vertex shader
    if (!loadShader(&vertexShader, GL_VERTEX_SHADER, vertexString)) {
        LOGE("Failed to compile vertex shader");
        return false;
    }

    // Create and compile fragment shader
    if (!loadShader(&fragmentShader, GL_FRAGMENT_SHADER, fragmentString)) {
        LOGE("Failed to compile fragment shader");
        return false;
    }

    mProgram = glCreateProgram();
    glAttachShader(mProgram, vertexShader);
    glAttachShader(mProgram, fragmentShader);
    glLinkProgram(mProgram);

    GLint status;
    glGetProgramiv(mProgram, GL_LINK_STATUS, &status);
    if (status == GL_FALSE) {
        LOGE("Failed to link program");
        return false;
    }

    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);

    return true;
}

bool GLProgram::loadShader(GLuint *shader, GLenum type, std::string shaderString)
{
    const GLchar *source = shaderString.c_str();
    if(!source) {
        LOGI("Failed to load vertex shader");
        return false;
    }

    GLint status;

    *shader = glCreateShader(type);
    glShaderSource(*shader, 1, &source, NULL);
    glCompileShader(*shader);

    glGetShaderiv(*shader, GL_COMPILE_STATUS, &status);

    if (status != GL_TRUE) {
        GLint logLength;
        glGetShaderiv(*shader, GL_INFO_LOG_LENGTH, &logLength);
        if (logLength > 0) {
            GLchar *log = (GLchar *)malloc(logLength);
            glGetShaderInfoLog(*shader, logLength, &logLength, log);
            LOGI("Shader log: %s", log);
            free(log);
        }
    }

    return status == GL_TRUE;
}

void GLProgram::useProgram()
{
    VAssert(mProgram != 0, "Bad program");
    glUseProgram(mProgram);
}

GLuint GLProgram::getUniformLocation(std::string uniformName)
{
    glGetUniformLocation(mProgram, uniformName.c_str());
}

GLuint GLProgram::getAttribLocation(std::string attributeName)
{
    glGetAttribLocation(mProgram, attributeName.c_str());
}