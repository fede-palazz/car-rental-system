import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useParams } from "react-router-dom";
import { NoteReqDTO } from "@/models/dtos/request/NoteReqDTO";
import NotesAPI from "@/API/NotesAPI";
import { Note } from "@/models/Note";
import { Textarea } from "@/components/ui/textarea";

const noteSchema = z.object({
  content: z.string().min(1, "Content must not be blank"),
});

export default function AddOrEditNoteDialog() {
  const navigate = useNavigate();
  const { vehicleId, noteId } = useParams<{
    vehicleId: string;
    noteId?: string;
  }>();

  const form = useForm<NoteReqDTO>({
    resolver: zodResolver(noteSchema),
    defaultValues: {
      content: "",
    },
  });

  useEffect(() => {
    if (!noteId) return;
    NotesAPI.getNoteById(Number(vehicleId), Number(noteId))
      .then((note: Note) => {
        form.reset(note, { keepDefaultValues: true });
      })
      .catch();
  }, [form, noteId, vehicleId]);

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    if (noteId !== undefined) {
      handleEdit(form.getValues());
    } else {
      handleCreate(form.getValues());
    }
  }

  const handleEdit = (values: NoteReqDTO) => {
    NotesAPI.editNoteById(Number(vehicleId), values, Number(noteId))
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleCreate = (values: NoteReqDTO) => {
    NotesAPI.createNote(Number(vehicleId), values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <Dialog
      open={true}
      onOpenChange={(isOpen) => {
        if (!isOpen) navigate(-1);
      }}>
      <DialogDescription></DialogDescription>
      <DialogContent
        className="sm:max-w-[425px] md:min-w-1/2 overflow-auto max-h-11/12"
        style={{
          scrollbarWidth: "none", // For Firefox
          msOverflowStyle: "none", // For IE and Edge
        }}>
        <DialogHeader>
          <DialogTitle>
            {noteId === undefined ? "Add Note" : "Edit Note"}
          </DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <FormField
              control={form.control}
              name="content"
              render={({ field }) => (
                <FormItem className="col-span-full">
                  <FormLabel>Content*</FormLabel>
                  <FormControl>
                    <Textarea
                      className="resize-none min-h-32"
                      startIcon={
                        <span className="material-symbols-outlined items-center md-18">
                          description
                        </span>
                      }
                      placeholder={"Content"}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter className="col-span-full ">
              <div className="flex items-center justify-between w-full">
                <Button
                  variant="secondary"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(-1);
                  }}>
                  <span className="material-symbols-outlined items-center md-18">
                    close
                  </span>
                  Cancel
                </Button>
                <Button
                  variant="default"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    onSubmit();
                  }}>
                  {noteId === undefined ? "Add" : "Edit"}
                  <span className="material-symbols-outlined  md-18">
                    {noteId === undefined ? "add" : "edit"}
                  </span>
                </Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
