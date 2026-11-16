import logo from "@/assets/logo.png";
import { cn } from "cn";

export function BrandLogo({ className }) {
    return (
        <img
            src={logo}
            alt=""
            aria-hidden="true"
            width={40}
            height={40}
            decoding="async"
            className={cn("shrink-0 object-contain", className)}
        />
    );
}
