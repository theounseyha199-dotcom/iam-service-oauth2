package kh.edu.iam.infrastructure.workspace;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/workspace/api/deactivation", "/api/console/deactivation"})
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Workspace")
public class DeactivationController {
    private final DeactivationService requests;

    public record Request(@NotBlank @Size(max = 500) String reason) {}

    public record Review(@NotNull DeactivationService.Decision decision, @NotBlank @Size(max = 500) String reason,
                         @NotNull @Min(0) Long version) {}

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @io.swagger.v3.oas.annotations.Operation(summary = "Request account deactivation")
    public void request(@Valid @RequestBody Request input) {
        requests.request(input.reason());
    }

    @GetMapping("/mine")
    @io.swagger.v3.oas.annotations.Operation(summary = "Read your deactivation decision")
    public Map<String, Object> mine() {
        return requests.mine();
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "List deactivation requests")
    public List<Map<String, Object>> list(@RequestParam(defaultValue = "", name = "status") String status) {
        return requests.list(status);
    }

    @PostMapping("/{subject}/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @io.swagger.v3.oas.annotations.Operation(summary = "Approve or reject a pending deactivation request")
    public void review(@PathVariable("subject") UUID subject, @Valid @RequestBody Review input) {
        requests.review(subject, input.decision(), input.reason(), input.version());
    }
}
