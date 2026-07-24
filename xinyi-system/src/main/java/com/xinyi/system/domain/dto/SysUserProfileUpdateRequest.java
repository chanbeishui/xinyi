package com.xinyi.system.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 员工自助资料更新，只包含非授权字段。
 */
public class SysUserProfileUpdateRequest
{
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称不能超过30个字符")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱不能超过50个字符")
    private String email;

    @Size(max = 11, message = "手机号码不能超过11个字符")
    private String phonenumber;

    @Size(max = 1, message = "性别代码格式不正确")
    private String sex;

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }
}
