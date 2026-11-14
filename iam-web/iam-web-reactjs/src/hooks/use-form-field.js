import { useFormContext, useFormState } from "react-hook-form";

export function useFormField(name, serverError) {
    const form = useFormContext();
    const { errors, dirtyFields } = useFormState({
        control: form.control,
        name,
    });
    return {
        registration: form.register(name),
        error:
            errors[name]?.message ??
            (!dirtyFields[name] ? serverError : undefined),
    };
}
