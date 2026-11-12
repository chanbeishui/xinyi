"""D02/D03/D04 同轮实际 HTTP 响应；按主源及原唯一任务归属切片验证，绝不自造响应。"""
from pathlib import Path
import json
import sys
import yaml
from jsonschema import Draft4Validator, FormatChecker


def adapt(value):
    if isinstance(value, list):
        return [adapt(item) for item in value]
    if not isinstance(value, dict):
        return value
    result = {key: adapt(item) for key, item in value.items()}
    return {"anyOf": [result, {"type": "null"}]} if result.pop("nullable", False) else result


root, evidence = map(Path, sys.argv[1:3])
actual = json.loads(evidence.read_text())
operations = [
    ("policy-get", "get", "/api/admin/dispatch/companies/{companyId}/dispatch-policy", "D02"),
    ("policy-put", "put", "/api/admin/dispatch/companies/{companyId}/dispatch-policy", "D02"),
    ("revoked", "post", "/api/admin/dispatch/invites/{inviteId}/revoke", "D02"),
    ("queue", "get", "/api/admin/dispatch/queue", "D03"),
    ("candidates", "get", "/api/admin/dispatch/candidates", "D03"),
    ("manual", "post", "/api/admin/dispatch/manual-invites", "D03"),
    ("history", "get", "/api/admin/dispatch/invites", "D03"),
    ("list", "get", "/api/app/driver/invites", "D04"),
    ("detail", "get", "/api/app/driver/invites/{inviteId}", "D04"),
    ("accepted", "post", "/api/app/driver/invites/{inviteId}/response", "D04"),
    ("current", "get", "/api/app/driver/assignments/current", "D04"),
]
main = "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml"
loaded = {}
for name, method, path, task in operations:
    for source in [main, f"docs/vehicle/V6.4/12_工程执行/task_packets/{task}/openapi.yaml"]:
        if source not in loaded:
            loaded[source] = adapt(yaml.safe_load((root / source).read_text()))
        spec = loaded[source]
        schema = dict(spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"])
        schema["components"] = spec["components"]
        Draft4Validator(schema, format_checker=FormatChecker()).validate(actual[name])
print(f"{len(operations)} actual HTTP responses conform to the main source and their D02/D03/D04 slices")
