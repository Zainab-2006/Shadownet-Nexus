import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const cyberCardVariants = cva(
  "relative rounded-lg transition-all duration-300",
  {
    variants: {
      variant: {
        default: [
          "bg-card/80 backdrop-blur-md border border-border/50",
          "shadow-card hover:shadow-elevated",
          "hover:border-border-glow/50",
        ].join(" "),
        elevated: [
          "bg-card-elevated/90 backdrop-blur-xl border border-border-glow/30",
          "shadow-elevated",
        ].join(" "),
        glass: [
          "glass-elevated",
          "hover:border-primary/30",
        ].join(" "),
        glow: [
          "bg-card/80 backdrop-blur-md border border-primary/30",
          "shadow-glow-subtle hover:shadow-glow-primary",
          "hover:border-primary/60",
        ].join(" "),
        interactive: [
          "bg-card/80 backdrop-blur-md border border-border/50",
          "shadow-card hover:shadow-elevated",
          "hover:border-primary/50 hover:scale-[1.02]",
          "cursor-pointer",
        ].join(" "),
        hero: [
          "bg-gradient-card backdrop-blur-xl border border-border-glow/40",
          "shadow-elevated",
        ].join(" "),
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

export interface CyberCardProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof cyberCardVariants> {}

const CyberCard = React.forwardRef<HTMLDivElement, CyberCardProps>(
  ({ className, variant, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(cyberCardVariants({ variant, className }))}
      {...props}
    />
  )
);
CyberCard.displayName = "CyberCard";

const CyberCardHeader = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex flex-col space-y-1.5 p-6", className)}
    {...props}
  />
));
CyberCardHeader.displayName = "CyberCardHeader";

const CyberCardTitle = React.forwardRef<
  HTMLHeadingElement,
  React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
  <h3
    ref={ref}
    className={cn("font-heading text-xl font-bold tracking-wide", className)}
    {...props}
  />
));
CyberCardTitle.displayName = "CyberCardTitle";

const CyberCardDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <p
    ref={ref}
    className={cn("text-sm text-muted-foreground", className)}
    {...props}
  />
));
CyberCardDescription.displayName = "CyberCardDescription";

const CyberCardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />
));
CyberCardContent.displayName = "CyberCardContent";

const CyberCardFooter = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn("flex items-center p-6 pt-0", className)}
    {...props}
  />
));
CyberCardFooter.displayName = "CyberCardFooter";

export {
  CyberCard,
  CyberCardHeader,
  CyberCardFooter,
  CyberCardTitle,
  CyberCardDescription,
  CyberCardContent,
  cyberCardVariants,
};
