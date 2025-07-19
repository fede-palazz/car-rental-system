import * as React from "react";

import { cn } from "@/lib/utils";

interface TextareaProps
  extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  startIcon?: React.ReactNode;
  endIcon?: React.ReactNode;
}

function Textarea({ className, startIcon, endIcon, ...props }: TextareaProps) {
  const StartIcon: React.ReactNode = startIcon;
  const EndIcon: React.ReactNode = endIcon;

  return (
    <div className="w-full relative">
      {StartIcon && (
        <div className="absolute left-1.5 top-[0.6rem] flex items-start text-muted-foreground">
          {
            StartIcon
            //peer-focus:text-gray-900
            /* <StartIcon size={18} className="text-muted-foreground" />*/
          }
        </div>
      )}
      <textarea
        data-slot="textarea"
        className={cn(
          "border-input placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive dark:bg-background flex min-h-16 w-full rounded-md border bg-background px-3 py-2 text-base shadow-xs transition-[color,box-shadow] outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50 md:text-sm align-middle",
          startIcon ? "pl-8" : "",
          endIcon ? "pr-8" : "",
          className
        )}
        {...props}
      />
      {EndIcon && (
        <div className="absolute right-3 top-1/2 transform flex items-center -translate-y-1/2 text-muted-foreground">
          {
            EndIcon
            /*<EndIcon size={18} className="text-muted-foreground" />*/
          }
        </div>
      )}
    </div>
  );
}

export { Textarea };
