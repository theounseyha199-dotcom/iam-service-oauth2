import { useFormContext } from "react-hook-form";
export function FormSelect({ name, ...props }) {
    const { register } = useFormContext();
    return <select {...props} {...register(name)} />;
}
