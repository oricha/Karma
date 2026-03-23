const ThemePill = ({
  label,
  active = false,
  onClick,
}: {
  label: string;
  active?: boolean;
  onClick?: () => void;
}) => {
  return (
    <button
      onClick={onClick}
      className={`pill-tag transition-all ${active ? 'pill-tag-active' : 'pill-tag-inactive'}`}
    >
      {label}
    </button>
  );
};

export default ThemePill;
