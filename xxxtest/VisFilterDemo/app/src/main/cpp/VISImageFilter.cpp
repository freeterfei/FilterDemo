//
// Created by 陈飞飞 on 2018/9/20.
//

#include "VISImageFilter.h"

const char *VISImageFilter::mVertexShader = \
        "attribute vec4 position;\n"
        "attribute vec4 inputTextureCoordinate;\n"
        "varying vec2 textureCoordinate;\n"
        "void main()\n"
        "{\n"
        "gl_Position = position;\n"
        "textureCoordinate = inputTextureCoordinate.xy;\n"
        "}";

const char *VISImageFilter::mFragmentShader = \
        "varying vec2 textureCoordinate;\n"
        "uniform sampler2D inputImageTexture;\n"
        "void main()\n"
        "{\n"
        "gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
        "}\n";

VISImageFilter::VISImageFilter()
{
    this->init();
}

VISImageFilter::~VISImageFilter()
{
    this->destroy();
}

void VISImageFilter::renderFrame(const GLfloat *vertexBuffer, const GLfloat *textureBuffer)
{
    mFilterProgram->useProgram();

    glVertexAttribPointer(mFilterProgram->getAttribLocation("position"), 2, GL_FLOAT, 0, 0, vertexBuffer);
    glEnableVertexAttribArray(mFilterProgram->getAttribLocation("position"));
    glVertexAttribPointer(mFilterProgram->getAttribLocation("inputTextureCoordinate"), 2, GL_FLOAT, 0, 0, textureBuffer);
    glEnableVertexAttribArray(mFilterProgram->getAttribLocation("inputTextureCoordinate"));

    if(textureId != -1) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(mFilterProgram->getUniformLocation("inputImageTexture"), 0);
    }
    glEnableVertexAttribArray()
    cubeBuffer.position(0);
    GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
    GLES20.glEnableVertexAttribArray(mGLAttribPosition);
    textureBuffer.position(0);
    GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
    GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
    if (textureId != OpenGlUtils.NO_TEXTURE) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);
    }
    onDrawArraysPre();
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    GLES20.glDisableVertexAttribArray(mGLAttribPosition);
    GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
    onDrawArraysAfter();
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
}

void VISImageFilter::sizeChange()
{

}

void VISImageFilter::init()
{
    this->init(mVertexShader, mFragmentShader);
}

void VISImageFilter::init(const std::string &vertexShader, const std::string &fragmentShader)
{
    mFilterProgram = new GLProgram();
    mFilterProgram->initProgram(vertexShader, fragmentShader);
}

void VISImageFilter::destroy()
{
    if(mFilterProgram != NULL) {
        delete mFilterProgram;
        mFilterProgram = NULL;
    }
}
