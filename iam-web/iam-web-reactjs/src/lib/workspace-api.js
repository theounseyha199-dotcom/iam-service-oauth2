export function createWorkspaceApi({ contextPath, csrfToken }) {
    return async (path, options = {}) => {
        const headers = {
            Accept: "application/json",
            ...(options.body
                ? {
                      "Content-Type": "application/json",
                  }
                : {}),
            ...(options.method && options.method !== "GET"
                ? {
                      "X-CSRF-TOKEN": csrfToken,
                  }
                : {}),
        };
        const response = await fetch(contextPath + "/workspace/api" + path, {
            ...options,
            credentials: "same-origin",
            headers,
            body: options.body ? JSON.stringify(options.body) : undefined,
        });
        if (!response.ok || response.redirected) {
            let problem = {};
            try {
                problem = await response.json();
            } catch {
                /* HTML responses never become UI copy. */
            }
            throw {
                status: response.redirected ? 401 : response.status,
                fields: problem.errors?.map((e) => e.field),
            };
        }
        if (
            response.status === 204 ||
            response.headers.get("content-length") === "0"
        )
            return null;
        return response.text().then((text) => (text ? JSON.parse(text) : null));
    };
}
