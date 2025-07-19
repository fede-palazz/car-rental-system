import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
  DialogHeader,
  DialogFooter,
} from "./ui/dialog";
import { Button } from "./ui/button";
import { useLocation } from "react-router-dom";

export default function ConfirmationDialog({
  open,
  handleSubmit,
  handleCancel,
  title,
  content,
  submitButtonLabel,
  submitButtonVariant,
  description,
  descriptionClassName,
}: {
  open: boolean;
  handleSubmit: () => void;
  handleCancel: () => void;
  title: string;
  content: string;
  submitButtonLabel: string;
  submitButtonVariant?:
    | "link"
    | "default"
    | "destructive"
    | "outline"
    | "secondary"
    | "ghost";
  description?: string;
  descriptionClassName?: string;
}) {
  const location = useLocation();

  return (
    <Dialog
      open={open}
      onOpenChange={(isOpen) => {
        if (!isOpen) handleCancel();
      }}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription className={descriptionClassName}>
            {description}
          </DialogDescription>
        </DialogHeader>
        {content}
        <DialogFooter>
          <div className="flex justify-between w-full">
            <Button
              variant="ghost"
              type="button"
              onClick={() => handleCancel()}>
              Cancel
            </Button>
            <Button
              variant={submitButtonVariant ? submitButtonVariant : "default"}
              type="button"
              onClick={() => handleSubmit()}>
              {submitButtonLabel}
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
