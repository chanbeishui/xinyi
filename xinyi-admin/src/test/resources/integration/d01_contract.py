"""D01六条实际HTTP响应同时校验主源和任务切片，空配置由独立业务用例检查。"""
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


root, directory = map(Path, sys.argv[1:3])
operations = [
    ("availability", "get", "/api/app/driver/availability"),
    ("listening", "patch", "/api/app/driver/listening"),
    ("location", "post", "/api/app/driver/location"),
    ("presence", "post", "/api/app/driver/presence"),
    ("config-get", "get", "/api/admin/dispatch/companies/{companyId}/listening-config"),
    ("config-put", "put", "/api/admin/dispatch/companies/{companyId}/listening-config"),
]
for source in ["docs/vehicle/V6.4/04_接口/openapi_V6.4.yaml", "docs/vehicle/V6.4/12_工程执行/task_packets/D01/openapi.yaml"]:
    spec = adapt(yaml.safe_load((root / source).read_text()))
    for name, method, path in operations:
        schema = spec["paths"][path][method]["responses"]["200"]["content"]["application/json"]["schema"]
        schema["components"] = spec["components"]
        Draft4Validator(schema, format_checker=FormatChecker()).validate(json.loads((directory / f"{name}.json").read_text()))
    print(f"6 actual responses conform to {source}")
