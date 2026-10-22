"""V6.4 reference decision functions. Pure unit model, NOT production authentication or concurrency proof."""
from dataclasses import dataclass
from typing import Iterable
@dataclass(frozen=True)
class Employee:
    user_id: int
    normalized_phone: str
    active: bool = True
    deleted: bool = False
@dataclass(frozen=True)
class Link:
    account_id: int
    employee_id: int
    status: str = "ACTIVE"
    auto_blocked: bool = False
@dataclass(frozen=True)
class Decision:
    status: str
    employee_id: int | None
    outcome: str
def decide(account_id: int, verified_phone: str | None, source: str,
           employees: Iterable[Employee], links: Iterable[Link],
           account_blocked: bool=False, allow_same_suspended: bool=False) -> Decision:
    if source != "WECHAT" or not verified_phone:
        return Decision("UNLINKED",None,"NOT_ATTEMPTED")
    links=list(links)
    mine=[l for l in links if l.account_id==account_id and l.status!="REVOKED"]
    if account_blocked or any(l.account_id==account_id and l.auto_blocked for l in links):
        return Decision("REVOKED",None,"REBIND_BLOCKED")
    unique={e.user_id:e for e in employees if e.active and not e.deleted and e.normalized_phone==verified_phone}
    if not unique:return Decision("SUSPENDED" if mine else "UNLINKED",None,"NO_MATCH")
    if len(unique)>1:return Decision("NEEDS_ADMIN",None,"AMBIGUOUS_PHONE")
    eid=next(iter(unique))
    if mine and any(l.employee_id!=eid for l in mine):return Decision("NEEDS_ADMIN",None,"ACCOUNT_ALREADY_LINKED")
    if any(l.employee_id==eid and l.account_id!=account_id and l.status!="REVOKED" for l in links):
        return Decision("NEEDS_ADMIN",None,"USER_ALREADY_LINKED")
    if mine:
        if mine[0].status=="SUSPENDED" and not allow_same_suspended:
            return Decision("SUSPENDED",eid,"EXISTING_LINK")
        return Decision("LINKED",eid,"EXISTING_LINK")
    return Decision("LINKED",eid,"LINKED")
def roles(driver_approved:bool, link_status:str, employee_active:bool,
          authorized_station_count:int, station_function_allowed:bool)->list[str]:
    result=["OWNER"]
    if driver_approved:result.append("DRIVER")
    if link_status=="LINKED" and employee_active and authorized_station_count>0 and station_function_allowed:result.append("STATION")
    return result
