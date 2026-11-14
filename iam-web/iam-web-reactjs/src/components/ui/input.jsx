import { cn } from "cn";

function Input({ className, type, density = "compact", ...props }) {
    return (
        <input
            type={type}
            data-slot="input"
            data-density={density}
            className={cn(
                "h-9 w-full min-w-0 rounded-md border border-input bg-transparent px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none selection:bg-primary selection:text-primary-foreground file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm dark:bg-input/30",
                "focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50",
                "aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40",
                density === "account" &&
                    "h-auto min-h-[3.125rem] rounded-lg bg-card px-3.5 py-3 text-[.9375rem] shadow-none hover:border-slate-400 focus-visible:border-primary focus-visible:ring-brand-100 aria-invalid:focus-visible:ring-destructive/20",
                type === "checkbox" &&
                    "size-4 shrink-0 appearance-auto p-0 accent-primary",
                className,
            )}
            {...props}
        />
    );
}

export { Input };
