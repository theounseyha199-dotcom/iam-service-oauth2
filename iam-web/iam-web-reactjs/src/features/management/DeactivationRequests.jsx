import { useWorkspace } from "@/context/workspace-context";
import { useResource } from "@/hooks/use-resource";
import { ResourceState } from "@/components/workspace/ResourceState";
import { Table } from "@/components/workspace/DataTable";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { ReviewDeactivation } from "@/features/management/ReviewDeactivation";

export function DeactivationRequests() {
    const { t, can, data, formatDate } = useWorkspace();
    const [status, setStatus] = useState("PENDING");
    const [review, setReview] = useState(null);
    const r = useResource(`/deactivation?status=${status}`);
    return (
        <section className="card block min-w-0 rounded-2xl border border-border bg-card p-[clamp(1.25rem,4vw,2rem)] shadow-none [.dashboard-root_&]:rounded-[10px] [.dashboard-root_&]:p-[1.4rem] workspace-section mt-6">
            <h2>{t("requests")}</h2>
            <p className="my-3 text-sm text-muted-foreground">
                {t("reviewQueueHint")}
            </p>
            <label className="my-3 block max-w-xs">
                {t("status")}
                <select
                    className="mt-2 block w-full"
                    value={status}
                    onChange={(event) => {
                        setStatus(event.target.value);
                        setReview(null);
                    }}
                >
                    {["PENDING", "APPROVED", "REJECTED", ""].map((value) => (
                        <option key={value} value={value}>
                            {t(value || "all")}
                        </option>
                    ))}
                </select>
            </label>
            <ResourceState resource={r}>
                {(rows) => (
                    <Table
                        rows={rows}
                        rowKey="subject"
                        columns={[
                            {
                                key: "username",
                            },
                            {
                                key: "reason",
                            },
                            {
                                key: "requested_at",
                                label: "timestamp",
                                className: "whitespace-nowrap!",
                                render: (row) => formatDate(row.requested_at),
                            },
                            {
                                key: "status",
                                className: "whitespace-nowrap!",
                                render: (row) => t(row.status),
                            },
                            ...(status !== "PENDING"
                                ? [
                                      {
                                          key: "review_note",
                                          label: "reviewReason",
                                      },
                                      {
                                          key: "reviewed_at",
                                          label: "reviewedAt",
                                          className: "whitespace-nowrap!",
                                          render: (row) =>
                                              row.reviewed_at
                                                  ? formatDate(row.reviewed_at)
                                                  : "—",
                                      },
                                  ]
                                : []),
                            ...(can("user:update") &&
                            (!status || status === "PENDING")
                                ? [
                                      {
                                          key: "actions",
                                          render: (row) =>
                                              row.status === "PENDING" &&
                                              row.subject !==
                                                  data.claims?.subject ? (
                                                  <div className="flex flex-wrap gap-2">
                                                      {[
                                                          "APPROVED",
                                                          "REJECTED",
                                                      ].map((decision) => (
                                                          <Button
                                                              key={decision}
                                                              variant="outline"
                                                              onClick={() =>
                                                                  setReview({
                                                                      request:
                                                                          row,
                                                                      decision,
                                                                  })
                                                              }
                                                          >
                                                              {t(
                                                                  decision ===
                                                                      "APPROVED"
                                                                      ? "approve"
                                                                      : "reject",
                                                              )}
                                                          </Button>
                                                      ))}
                                                  </div>
                                              ) : null,
                                      },
                                  ]
                                : []),
                        ]}
                    />
                )}
            </ResourceState>
            {review && (
                <ReviewDeactivation
                    key={`${review.request.subject}-${review.request.version}-${review.decision}`}
                    {...review}
                    onCancel={() => setReview(null)}
                    onDone={() => {
                        setReview(null);
                        r.reload();
                    }}
                />
            )}
        </section>
    );
}
