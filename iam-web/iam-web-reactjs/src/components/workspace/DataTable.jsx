import { useWorkspace } from "@/context/workspace-context";
import {
    Table as ShadcnTable,
    TableHeader,
    TableRow,
    TableHead,
    TableBody,
    TableCell,
} from "@/components/ui/table";

export function Table({ columns, rows, rowKey = "uuid" }) {
    const { t } = useWorkspace();
    return (
        <div
            className="workspace-table my-4 min-h-[220px] rounded-[9px] border border-border bg-card [&_[data-slot=table-container]]:rounded-[inherit] [&_[data-slot=table-container]]:[scrollbar-gutter:stable] [&_table]:min-w-[720px] [&_table]:border-collapse [&_th]:bg-slate-50 [&_th]:text-[.7rem] [&_th]:font-medium [&_th]:text-slate-600 [&_th]:px-4 [&_th]:py-[.8rem] [&_th]:text-start [&_th]:max-w-[340px] [&_th]:whitespace-normal [&_th]:wrap-anywhere [&_td]:px-4 [&_td]:py-[.8rem] [&_td]:text-start [&_td]:max-w-[340px] [&_td]:whitespace-normal [&_td]:wrap-anywhere [&_td]:text-[.77rem] [&_td]:align-middle [&_td_code]:text-[.65rem] [&_td_code]:break-all [&_td_code]:text-slate-500"
            tabIndex={0}
            role="region"
            aria-label={t("records")}
        >
            <ShadcnTable>
                <TableHeader>
                    <TableRow>
                        {columns.map((c) => (
                            <TableHead
                                key={c.key}
                                scope="col"
                                className={c.className}
                            >
                                {t(c.label ?? c.key)}
                            </TableHead>
                        ))}
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {rows.length ? (
                        rows.map((row, i) => (
                            <TableRow key={row[rowKey] ?? i}>
                                {columns.map((c) => (
                                    <TableCell
                                        key={c.key}
                                        className={c.className}
                                    >
                                        {c.render
                                            ? c.render(row)
                                            : String(row[c.key] ?? "—")}
                                    </TableCell>
                                ))}
                            </TableRow>
                        ))
                    ) : (
                        <TableRow>
                            <TableCell
                                colSpan={columns.length}
                                className="workspace-empty text-center! p-12!"
                            >
                                {t("empty")}
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </ShadcnTable>
        </div>
    );
}
