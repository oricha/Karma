import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import AccountLayout from '@/components/layout/AccountLayout';
import { api } from '@/lib/api';

const OrdersPage = () => {
  const { t } = useTranslation('account');
  const { data: orders = [] } = useQuery({ queryKey: ['orders'], queryFn: api.getOrders });

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('orders.title')}</h1>
      {orders.length === 0 ? (
        <div className="bg-card rounded-xl p-8 text-center">
          <p className="font-body text-muted-foreground">{t('orders.empty')}</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="bg-card rounded-xl p-4">
              <p className="font-body font-semibold">{order.event?.title}</p>
              <p className="text-sm text-muted-foreground">{order.totalAmount} {order.currency}</p>
            </div>
          ))}
        </div>
      )}
    </AccountLayout>
  );
};

export default OrdersPage;
