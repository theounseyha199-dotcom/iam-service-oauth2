import { useFormContext } from "react-hook-form";
import { Input } from "@/components/ui/input";
export function FormCheckbox({ name, ...props }) {
    const { register } = useFormContext();
    return <Input {...props} type="checkbox" {...register(name)} />;
}
