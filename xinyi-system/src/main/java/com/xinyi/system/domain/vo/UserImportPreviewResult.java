package com.xinyi.system.domain.vo;

/**
 * Excel用户导入预检摘要。
 */
public class UserImportPreviewResult
{
    private String previewToken;
    private int rowCount;
    private int createCount;
    private int updateCount;
    private int platformAccountCount;
    private long expiresInSeconds;

    public String getPreviewToken()
    {
        return previewToken;
    }

    public void setPreviewToken(String previewToken)
    {
        this.previewToken = previewToken;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }

    public int getCreateCount()
    {
        return createCount;
    }

    public void setCreateCount(int createCount)
    {
        this.createCount = createCount;
    }

    public int getUpdateCount()
    {
        return updateCount;
    }

    public void setUpdateCount(int updateCount)
    {
        this.updateCount = updateCount;
    }

    public int getPlatformAccountCount()
    {
        return platformAccountCount;
    }

    public void setPlatformAccountCount(int platformAccountCount)
    {
        this.platformAccountCount = platformAccountCount;
    }

    public long getExpiresInSeconds()
    {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds)
    {
        this.expiresInSeconds = expiresInSeconds;
    }
}
