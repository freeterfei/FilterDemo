//
// Created by 陈飞飞 on 2018/9/20.
//

#ifndef VISFILTERDEMO_VISIMAGEFILTER_H
#define VISFILTERDEMO_VISIMAGEFILTER_H
#include "GLProgram.h"

class VISImageFilter
{
public:
    VISImageFilter();
    virtual ~VISImageFilter();

    virtual void renderFrame();
    virtual void sizeChange();

protected:
    virtual void init();
    virtual void destroy();

private:
    virtual void init(const std::string& vertexShader, const std::string& fragmentShader);

protected:
    static const char *mVertexShader;
    static const char *mFragmentShader;

private:

    GLProgram *mFilterProgram;
};


#endif //VISFILTERDEMO_VISIMAGEFILTER_H
