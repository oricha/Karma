import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

const EmptyState = ({ message, linkText, linkTo }: { message: string; linkText?: string; linkTo?: string }) => {
  const { t } = useTranslation();
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <p className="text-muted-foreground text-lg mb-4">{message}</p>
      {linkText && linkTo && (
        <Link to={linkTo} className="text-primary hover:text-primary-hover font-medium underline">
          {linkText}
        </Link>
      )}
    </div>
  );
};

export default EmptyState;
