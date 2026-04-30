export function Logo({ height = 40, accentColor = true }: { height?: number, accentColor?: boolean } = {}) {
  return (
    <div className="flex items-center gap-0 select-none">
      <div
        className="flex items-center leading-none"
        style={{
          fontFamily: "Nunito, sans-serif",
          fontSize: `${height * 0.8}px`,
          height: `${height}px`,
          fontWeight: 900,
        }}
      >
        <span className="text-foreground">ticket</span>
        <span className={accentColor ? "text-accent" : "text-foreground"}>rush</span>
      </div>
      <SquareLogo height={height} accentColor={accentColor} />
    </div>
  );
}

export function SquareLogo({ height = 40, accentColor = true }: { height?: number, accentColor?: boolean } = {}) {
  return (
    <svg
      className={`w-10 h-10 select-none ${accentColor ? 'fill-accent' : 'fill-foreground'}`}
      fill="currentColor"
      viewBox="0 0 24 24"
      style={{ height: `${height}px`, width: `${height}px`}}
    >
      <path d="M5.055 7.06C3.805 6.347 2.25 7.25 2.25 8.69v8.122c0 1.44 1.555 2.343 2.805 1.628L12 14.471v2.34c0 1.44 1.555 2.343 2.805 1.628l7.108-4.061c1.26-.72 1.26-2.536 0-3.256l-7.108-4.061C13.555 6.346 12 7.249 12 8.689v2.34L5.055 7.061Z" />
    </svg>
  );
}
