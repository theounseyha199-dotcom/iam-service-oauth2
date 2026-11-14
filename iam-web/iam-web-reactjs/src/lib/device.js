// User-Agent describes the client; it is display metadata, never authentication evidence.
export function describeDevice(userAgent = "") {
    const ua = String(userAgent);
    const browser = /Edg(A|iOS)?\//.test(ua)
        ? "Microsoft Edge"
        : /OPR\/|Opera/.test(ua)
          ? "Opera"
          : /Firefox\/|FxiOS\//.test(ua)
            ? "Firefox"
            : /Chrome\/|CriOS\//.test(ua)
              ? "Chrome"
              : /Safari\//.test(ua)
                ? "Safari"
                : "";
    const os = /Android/.test(ua)
        ? "Android"
        : /iPhone|iPad|iPod/.test(ua)
          ? "iOS"
          : /Windows/.test(ua)
            ? "Windows"
            : /Macintosh|Mac OS X/.test(ua)
              ? "macOS"
              : /Linux/.test(ua)
                ? "Linux"
                : "";
    const kind =
        /iPad|Tablet/.test(ua) || (/Android/.test(ua) && !/Mobile/.test(ua))
            ? "tablet"
            : /Mobile|iPhone|iPod/.test(ua)
              ? "mobile"
              : os
                ? "computer"
                : "unknown";
    return { browser, os, kind };
}
