const DecorativeBlob = ({ 
  className = '', 
  variant = 'primary' 
}: { 
  className?: string; 
  variant?: 'primary' | 'secondary' | 'accent';
}) => {
  const colorMap = {
    primary: 'bg-primary/20',
    secondary: 'bg-secondary/20',
    accent: 'bg-amber/20',
  };

  return (
    <div className={`blob-decoration animate-blob-move ${colorMap[variant]} ${className}`} />
  );
};

export default DecorativeBlob;
