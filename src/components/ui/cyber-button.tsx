import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const cyberButtonVariants = cva(
  "relative inline-flex items-center justify-center gap-2 whitespace-nowrap font-heading text-sm font-semibold uppercase tracking-wider transition-all duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 overflow-hidden",
  {
    variants: {
      variant: {
        primary: [
          "bg-gradient-primary text-primary-foreground",
          "shadow-glow-primary hover:shadow-[0_0_30px_hsl(185_100%_50%/0.6)]",
          "hover:scale-[1.02] active:scale-[0.98]",
          "before:absolute before:inset-0 before:bg-gradient-to-r before:from-transparent before:via-white/20 before:to-transparent",
          "before:translate-x-[-200%] hover:before:translate-x-[200%] before:transition-transform before:duration-700",
        ].join(" "),
        secondary: [
          "bg-gradient-secondary text-secondary-foreground",
          "shadow-glow-secondary hover:shadow-[0_0_30px_hsl(320_100%_60%/0.6)]",
          "hover:scale-[1.02] active:scale-[0.98]",
          "before:absolute before:inset-0 before:bg-gradient-to-r before:from-transparent before:via-white/20 before:to-transparent",
          "before:translate-x-[-200%] hover:before:translate-x-[200%] before:transition-transform before:duration-700",
        ].join(" "),
        outline: [
          "border-2 border-primary bg-transparent text-primary",
          "hover:bg-primary/10 hover:shadow-glow-subtle",
          "hover:scale-[1.02] active:scale-[0.98]",
        ].join(" "),
        ghost: [
          "bg-transparent text-foreground hover:bg-accent hover:text-accent-foreground",
          "hover:shadow-glow-subtle",
        ].join(" "),
        destructive: [
          "bg-destructive text-destructive-foreground",
          "hover:bg-destructive/90 hover:shadow-[0_0_20px_hsl(0_85%_55%/0.5)]",
          "hover:scale-[1.02] active:scale-[0.98]",
        ].join(" "),
        success: [
          "bg-success text-success-foreground",
          "hover:shadow-glow-success hover:scale-[1.02] active:scale-[0.98]",
        ].join(" "),
        hero: [
          "bg-gradient-primary text-primary-foreground",
          "shadow-glow-primary hover:shadow-[0_0_40px_hsl(185_100%_50%/0.7)]",
          "hover:scale-[1.03] active:scale-[0.97]",
          "before:absolute before:inset-0 before:bg-gradient-to-r before:from-transparent before:via-white/30 before:to-transparent",
          "before:translate-x-[-200%] hover:before:translate-x-[200%] before:transition-transform before:duration-500",
          "after:absolute after:inset-0 after:bg-gradient-to-t after:from-black/20 after:to-transparent",
        ].join(" "),
      },
      size: {
        default: "h-10 px-6 py-2",
        sm: "h-8 px-4 text-xs",
        lg: "h-12 px-8 text-base",
        xl: "h-14 px-10 text-lg",
        icon: "h-10 w-10",
      },
      shape: {
        default: "rounded-md",
        cyber: "clip-cyber-sm",
        pill: "rounded-full",
      },
    },
    defaultVariants: {
      variant: "primary",
      size: "default",
      shape: "default",
    },
  }
);

export interface CyberButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof cyberButtonVariants> {
  asChild?: boolean;
}

const CyberButton = React.forwardRef<HTMLButtonElement, CyberButtonProps>(
  ({ className, variant, size, shape, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button";
    return (
      <Comp
        className={cn(cyberButtonVariants({ variant, size, shape, className }))}
        ref={ref}
        {...props}
      />
    );
  }
);
CyberButton.displayName = "CyberButton";

export { CyberButton, cyberButtonVariants };
