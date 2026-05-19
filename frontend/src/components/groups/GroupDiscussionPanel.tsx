import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Pin, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { api } from '@/lib/api';
import { useSession } from '@/hooks/use-session';
import type { GroupPost } from '@/types';

interface GroupDiscussionPanelProps {
  groupId: string;
  isOrganizer?: boolean;
}

const GroupDiscussionPanel = ({ groupId, isOrganizer }: GroupDiscussionPanelProps) => {
  const { t } = useTranslation('groups');
  const { isLoggedIn, user } = useSession();
  const queryClient = useQueryClient();
  const [content, setContent] = useState('');
  const [replyDrafts, setReplyDrafts] = useState<Record<string, string>>({});

  const { data: posts = [], isLoading } = useQuery({
    queryKey: ['group-posts', groupId],
    queryFn: () => api.getGroupPosts(groupId),
    enabled: isLoggedIn,
  });

  const createPost = useMutation({
    mutationFn: () => api.createGroupPost(groupId, { content: content.trim() }),
    onSuccess: () => {
      setContent('');
      void queryClient.invalidateQueries({ queryKey: ['group-posts', groupId] });
    },
  });

  const replyToPost = useMutation({
    mutationFn: ({ postId, text }: { postId: string; text: string }) =>
      api.replyToGroupPost(groupId, postId, { content: text }),
    onSuccess: (_, variables) => {
      setReplyDrafts(prev => ({ ...prev, [variables.postId]: '' }));
      void queryClient.invalidateQueries({ queryKey: ['group-posts', groupId] });
    },
  });

  const deletePost = useMutation({
    mutationFn: (postId: string) => api.deleteGroupPost(groupId, postId),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['group-posts', groupId] }),
  });

  const pinPost = useMutation({
    mutationFn: ({ postId, pinned }: { postId: string; pinned: boolean }) =>
      api.pinGroupPost(groupId, postId, pinned),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['group-posts', groupId] }),
  });

  if (!isLoggedIn) {
    return (
      <p className="font-body text-muted-foreground">
        {t('discussion.loginRequired', { defaultValue: 'Inicia sesión para ver el foro del grupo.' })}
      </p>
    );
  }

  if (isLoading) {
    return <p className="font-body text-muted-foreground">{t('common:noResults')}</p>;
  }

  return (
    <section className="space-y-6">
      <div className="bg-card rounded-2xl p-4 space-y-3">
        <Textarea
          value={content}
          onChange={event => setContent(event.target.value)}
          placeholder={t('discussion.writePost')}
          rows={3}
        />
        <Button
          className="rounded-full"
          disabled={!content.trim() || createPost.isPending}
          onClick={() => createPost.mutate()}
        >
          {t('discussion.publish', { defaultValue: 'Publicar' })}
        </Button>
      </div>

      <ul className="space-y-4 list-none p-0 m-0">
        {posts.map((post: GroupPost) => (
          <li key={post.id} className="bg-card rounded-2xl p-4">
            <header className="flex items-start justify-between gap-3 mb-2">
              <p className="font-body font-semibold text-sm">
                {post.author.firstName} {post.author.lastName}
                {post.pinned ? <Pin className="inline h-3 w-3 ml-1 text-primary" /> : null}
              </p>
              <div className="flex gap-1">
                {isOrganizer ? (
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => pinPost.mutate({ postId: post.id, pinned: !post.pinned })}
                    aria-label="Pin post"
                  >
                    <Pin className="h-4 w-4" />
                  </Button>
                ) : null}
                {(isOrganizer || post.author.id === user?.id) ? (
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => deletePost.mutate(post.id)}
                    aria-label="Delete post"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                ) : null}
              </div>
            </header>
            <p className="font-body text-sm whitespace-pre-wrap mb-4">{post.content}</p>
            {post.replies?.length ? (
              <ul className="space-y-2 border-l-2 border-muted pl-4 mb-4 list-none">
                {post.replies.map(reply => (
                  <li key={reply.id} className="font-body text-sm">
                    <span className="font-semibold">{reply.author.firstName}: </span>
                    {reply.content}
                  </li>
                ))}
              </ul>
            ) : null}
            <div className="flex gap-2 items-end">
              <Textarea
                value={replyDrafts[post.id] ?? ''}
                onChange={event => setReplyDrafts(prev => ({ ...prev, [post.id]: event.target.value }))}
                placeholder={t('discussion.reply', { defaultValue: 'Responder...' })}
                rows={2}
                className="text-sm"
              />
              <Button
                variant="outline"
                className="rounded-full shrink-0"
                disabled={!(replyDrafts[post.id]?.trim()) || replyToPost.isPending}
                onClick={() => replyToPost.mutate({ postId: post.id, text: replyDrafts[post.id]!.trim() })}
              >
                {t('discussion.reply', { defaultValue: 'Responder' })}
              </Button>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default GroupDiscussionPanel;
