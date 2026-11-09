"""Q04十一个设备认证/人工接口的真实响应，分别按主源与任务切片校验。"""
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


root, samples = map(Path, sys.argv[1:3])
operations = [
    ("face-create", "post", "/api/app/driver/qualification/face-session"),
    ("face-confirm", "post", "/api/app/driver/qualification/face-sessions/{verificationSessionId}/confirm"),
    ("face-get", "get", "/api/app/driver/qualification/face-sessions/{verificationSessionId}"),
    ("manual-candidates", "get", "/api/admin/manual-verifications/applications"),
    ("manual-list", "get", "/api/admin/manual-verifications"),
    ("manual-detail", "get", "/api/admin/manual-verifications/{id}"),
    ("manual-create", "post", "/api/admin/manual-verifications"),
    ("manual-review", "post", "/api/admin/manual-verifications/{id}/review"),
    ("admin-list", "get", "/api/admin/verification-sessions"),
    ("admin-detail", "get", "/api/admin/verification-sessions/{id}"),
    ("admin-close", "post", "/api/admin/verification-sessions/{id}/close"),
]
for source in [
    "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml",
    "docs/vehicle/V6.4/12_工程执行/task_packets/Q04/openapi.yaml",
]:
    spec = adapt(yaml.safe_load((root / source).read_text()))
    for name, method, path in operations:
        schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
        schema["components"] = spec["components"]
        Draft4Validator(schema, format_checker=FormatChecker()).validate(
            json.loads((samples / f"{name}.json").read_text()))
    if source.endswith("openapi_V6.4.yaml"):
        for name in ["public-config", "public-config-null"]:
            schema = spec["paths"]["/api/public/service-config"]["get"]["responses"]["200"]["content"]["application/json"]["schema"]
            schema["components"] = spec["components"]
            Draft4Validator(schema, format_checker=FormatChecker()).validate(
                json.loads((samples / f"{name}.json").read_text()))
    print(f"{len(operations)} actual Q04 HTTP responses conform to {source}")
