export function Hidden({ name, value }) {
    return <input type="hidden" name={name} value={value ?? ""} />;
}
