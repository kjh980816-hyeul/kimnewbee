import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import PostArticle from '@/components/post/PostArticle.vue';

const baseProps = {
  title: '제목 테스트',
  author: '작성자',
  createdAt: '2026-04-30T12:00:00.000Z',
  viewCount: 42,
  content: '본문 내용',
  likedByMe: false,
  likeCount: 7,
};

describe('PostArticle', () => {
  it('renders title, author, viewCount, content, likeCount', () => {
    const wrapper = mount(PostArticle, { props: baseProps });
    const text = wrapper.text();
    expect(text).toContain('제목 테스트');
    expect(text).toContain('작성자');
    expect(text).toContain('조회 42');
    expect(text).toContain('본문 내용');
    expect(text).toContain('7');
  });

  it('emits "like" when like button is clicked', async () => {
    const wrapper = mount(PostArticle, { props: baseProps });
    await wrapper.find('button').trigger('click');
    expect(wrapper.emitted('like')).toBeTruthy();
    expect(wrapper.emitted('like')).toHaveLength(1);
  });

  it('applies cheek active style when likedByMe is true', () => {
    const wrapper = mount(PostArticle, {
      props: { ...baseProps, likedByMe: true },
    });
    const classes = wrapper.find('button').classes();
    expect(classes).toContain('bg-cheek');
    expect(classes).toContain('text-paper');
  });

  it('renders media slot when provided', () => {
    const wrapper = mount(PostArticle, {
      props: baseProps,
      slots: { media: '<img alt="test" data-testid="media" />' },
    });
    expect(wrapper.find('[data-testid="media"]').exists()).toBe(true);
  });

  it('hides content paragraph when content is empty', () => {
    const wrapper = mount(PostArticle, {
      props: { ...baseProps, content: '' },
    });
    const paragraphs = wrapper.findAll('p');
    expect(paragraphs.every((p) => p.text() !== '본문 내용')).toBe(true);
  });
});
