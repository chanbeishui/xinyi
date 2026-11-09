"""校验 Q02 五个后台接口的真实 HTTP 响应，主源与任务切片分别核验。"""
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
    if result.pop("nullable", False):
        return {"anyOf": [result, {"type": "null"}]}
    return result


root, samples = map(Path, sys.argv[1:3])
operations = [
    ("list", "get", "/api/admin/training-courses"),
    ("create", "post", "/api/admin/training-courses"),
    ("detail", "get", "/api/admin/training-courses/{trainingId}"),
    ("save", "put", "/api/admin/training-courses/{trainingId}"),
    ("publish", "post", "/api/admin/training-courses/{trainingId}/publish"),
]
sources = [
    "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml",
    "docs/vehicle/V6.4/12_工程执行/task_packets/Q02/openapi.yaml",
]
for source in sources:
    spec = adapt(yaml.safe_load((root / source).read_text()))
    for name, method, path in operations:
        schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
        schema["components"] = spec["components"]
        response = json.loads((samples / f"{name}.json").read_text())
        Draft4Validator(schema, format_checker=FormatChecker()).validate(response)
    print(f"{len(operations)} actual Q02 HTTP responses conform to {source}")
