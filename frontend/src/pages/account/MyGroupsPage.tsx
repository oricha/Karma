import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import AccountLayout from '@/components/layout/AccountLayout';
import { Link } from 'react-router-dom';
import { Users, MapPin } from 'lucide-react';
import { api } from '@/lib/api';

const MyGroupsPage = () => {
  const { t } = useTranslation('account');
  const queryClient = useQueryClient();
  const { data: groups = [] } = useQuery({ queryKey: ['my-groups'], queryFn: api.getMyGroups });
  const mutation = useMutation({
    mutationFn: ({ groupId, preference }: { groupId: string; preference: 'IMMEDIATE' | 'DIGEST' | 'NEVER' }) =>
      api.updateGroupNotification(groupId, preference),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['my-groups'] });
    },
  });

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('myGroups.title')}</h1>
      <div className="space-y-4">
        {groups.map(group => (
          <div key={group.id} className="flex items-center gap-4 p-4 bg-card rounded-xl hover:shadow-sm transition-all">
            <Link to={`/groups/${group.slug}`} className="block shrink-0">
              <img src={group.bannerUrl} alt={group.name} className="w-16 h-16 rounded-lg object-cover" />
            </Link>
            <div className="flex-1">
              <Link to={`/groups/${group.slug}`} className="block">
                <h3 className="font-body font-semibold">{group.name}</h3>
                <div className="flex gap-3 text-xs text-muted-foreground font-body mt-1">
                  <span className="flex items-center gap-1"><Users className="h-3 w-3" />{group.memberCount}</span>
                  {group.city && <span className="flex items-center gap-1"><MapPin className="h-3 w-3" />{group.city}</span>}
                </div>
              </Link>
              <div className="mt-3">
                <label className="mb-1 block text-xs text-muted-foreground">{t('myGroups.notifications')}</label>
                <select
                  className="flex h-9 rounded-md border border-input bg-background px-3 py-1 text-sm"
                  value={group.notificationPreference ?? 'IMMEDIATE'}
                  onChange={(event) =>
                    mutation.mutate({
                      groupId: group.id,
                      preference: event.target.value as 'IMMEDIATE' | 'DIGEST' | 'NEVER',
                    })
                  }
                >
                  <option value="IMMEDIATE">{t('myGroups.immediate')}</option>
                  <option value="DIGEST">{t('myGroups.digest')}</option>
                  <option value="NEVER">{t('myGroups.never')}</option>
                </select>
              </div>
            </div>
          </div>
        ))}
      </div>
      <Link to="/groups" className="inline-block mt-6 text-primary hover:text-primary-hover font-body text-sm font-medium">
        {t('myGroups.browseMore')} →
      </Link>
    </AccountLayout>
  );
};

export default MyGroupsPage;
