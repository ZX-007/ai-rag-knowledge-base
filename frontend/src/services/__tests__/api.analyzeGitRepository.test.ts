import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ApiService } from '../../services/api';

describe('ApiService.analyzeGitRepository', () => {
  const endpoint = '/api/v1/rag/analyze_git_repository';

  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('应成功调用接口并返回结果字符串', async () => {
    const mockResponse = {
      code: '2000',
      info: 'OK',
      data: 'Git仓库分析完成！项目：demo，处理文件数：10，生成文档块数：100，耗时：123ms',
      timestamp: 'now'
    };

    (fetch as any).mockResolvedValue({
      json: () => Promise.resolve(mockResponse),
    });

    const result = await ApiService.analyzeGitRepository({
      repoUrl: 'https://github.com/org/demo.git',
      userName: 'user',
      token: 'token',
    });

    expect(fetch).toHaveBeenCalledWith(endpoint, expect.objectContaining({
      method: 'POST',
    }));
    expect(result).toContain('Git仓库分析完成');
  });

  it('当后端返回非2000状态时应抛出错误', async () => {
    const mockResponse = {
      code: '5000',
      info: '错误信息',
      data: '',
      timestamp: 'now'
    };
    (fetch as any).mockResolvedValue({ json: () => Promise.resolve(mockResponse) });

    await expect(ApiService.analyzeGitRepository({ repoUrl: 'https://example.com/repo.git' }))
      .rejects
      .toThrow('错误信息');
  });
});