package com.xinyi.rongli.support;

/**
 * 小程序请求级身份上下文。拦截器写入，Controller/Service 读取，请求结束清理。
 */
public final class MiniContext
{
    private static final ThreadLocal<MiniPrincipal> HOLDER = new ThreadLocal<>();

    private MiniContext()
    {
    }

    public static void set(MiniPrincipal principal)
    {
        HOLDER.set(principal);
    }

    public static MiniPrincipal get()
    {
        return HOLDER.get();
    }

    public static MiniPrincipal require()
    {
        MiniPrincipal p = HOLDER.get();
        if (p == null)
        {
            throw ApiException.unauthorized("登录状态已失效，请重新进入小程序");
        }
        return p;
    }

    public static void clear()
    {
        HOLDER.remove();
    }
}
