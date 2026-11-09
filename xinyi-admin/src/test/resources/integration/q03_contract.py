"""逐一校验 Q03 学习、阅卷及公司政策的真实 HTTP 响应，主源与任务切片分别核验。"""
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
    ("list", "get", "/api/app/driver/qualification/training"),
    ("detail", "get", "/api/app/driver/qualification/training/{trainingId}"),
    ("playback", "post", "/api/app/driver/qualification/training/{trainingId}/playback"),
    ("progress", "post", "/api/app/driver/qualification/training/{trainingId}/progress"),
    ("attempt-create", "post", "/api/app/driver/qualification/training/{trainingId}/attempts"),
    ("attempt-get", "get", "/api/app/driver/qualification/training/attempts/{attemptId}"),
    ("submit", "post", "/api/app/driver/qualification/training/submit"),
    ("policy-get", "get", "/api/admin/training-policies/{ownerDeptId}"),
    ("policy-save", "put", "/api/admin/training-policies/{ownerDeptId}"),
    ("policy-publish", "post", "/api/admin/training-policies/{ownerDeptId}/publish"),
]
for source in [
    "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml",
    "docs/vehicle/V6.4/12_工程执行/task_packets/Q03/openapi.yaml",
]:
    spec = adapt(yaml.safe_load((root / source).read_text()))
    for name, method, path in operations:
        schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
        schema["components"] = spec["components"]
        Draft4Validator(schema, format_checker=FormatChecker()).validate(
            json.loads((samples / f"{name}.json").read_text()))
    print(f"{len(operations)} actual Q03 HTTP responses conform to {source}")
