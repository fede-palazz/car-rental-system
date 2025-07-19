import * as React from "react";

import { cn } from "@/lib/utils";

export interface InputProps
  extends React.InputHTMLAttributes<HTMLInputElement> {
  startIcon?: React.ReactNode;
  endIcon?: React.ReactNode;
}

function Input({ className, type, startIcon, endIcon, ...props }: InputProps) {
  const StartIcon: React.ReactNode = startIcon;
  const EndIcon: React.ReactNode = endIcon;

  return !startIcon && !endIcon ? (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "file:text-foreground placeholder:text-muted-foreground selection:bg-primary selection:text-primary-foreground dark:bg-input/30 border-input flex h-9 w-full min-w-0 rounded-md border bg-background px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
        "focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px]",
        "aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
        className
      )}
      {...props}
    />
  ) : (
    <div className="w-full relative">
      {StartIcon && (
        <div className="absolute left-1.5 top-1/2 -translate-y-1/2 flex items-center text-muted-foreground">
          {
            StartIcon
            //peer-focus:text-gray-900
            /* <StartIcon size={18} className="text-muted-foreground" />*/
          }
        </div>
      )}
      <input
        type={type}
        className={cn(
          "peer flex h-10 w-full rounded-md border border-input bg-background py-2 px-4 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring focus-visible:ring-offset-0 disabled:cursor-not-allowed disabled:opacity-50",
          startIcon ? "pl-8" : "",
          endIcon ? "pr-8" : "",
          className
        )}
        //ref={ref}
        {...props}
      />
      {EndIcon && (
        <div className="absolute right-3 top-1/2 transform flex items-center -translate-y-1/2 text-muted-foreground">
          {
            type !== "password" && EndIcon
            /*<EndIcon size={18} className="text-muted-foreground" />*/
          }
        </div>
      )}
    </div>
  );
}

export { Input };
