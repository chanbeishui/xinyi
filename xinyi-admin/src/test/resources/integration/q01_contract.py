"""校验 Q01 四个真实 HTTP 响应；敏感表单、公开审查意见与生效资格分别约束。"""
from pathlib import Path
import json
import sys
import yaml
from jsonschema import Draft4Validator, FormatChecker

root, out = map(Path, sys.argv[1:3])
mode = sys.argv[3] if len(sys.argv) > 3 else "qualification"


def adapt(value):
    if isinstance(value, list):
        return [adapt(item) for item in value]
    if not isinstance(value, dict):
        return value
    result = {key: adapt(item) for key, item in value.items()}
    return {"anyOf": [result, {"type": "null"}]} if result.pop("nullable", False) else result


spec = adapt(yaml.safe_load((root / "docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml").read_text()))
operations = [
    ("create", "post", "/api/app/driver/qualification"),
    ("get", "get", "/api/app/driver/qualification"),
    ("save", "put", "/api/app/driver/qualification"),
    ("submit", "post", "/api/app/driver/qualification/submit"),
]
if mode == "public-missing":
    operations = [("public-missing", "get", "/api/public/service-config")]
for name, method, path in operations:
    schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
    schema["components"] = spec["components"]
    Draft4Validator(schema, format_checker=FormatChecker()).validate(
        json.loads((out / (name + ".json")).read_text()))
print(f"{len(operations)} actual {mode} HTTP responses conform to primary OpenAPI")
