package com.xinyi.rongli.booking.service;
import com.xinyi.rongli.booking.mapper.ScheduleMapper;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.platform.error.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
public class ScheduleService {
 private static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
 private final ScheduleMapper mapper;private final BusinessActorResolver actors;
 public ScheduleService(ScheduleMapper mapper,BusinessActorResolver actors){this.mapper=mapper;this.actors=actors;}
 public record Slot(String slotId,String startAt,String endAt,String displayText,boolean candidateOnly,String status){}
 public record Page(int code,String msg,List<Slot> rows,long total){}
 public Page list(String date,String area,int page,int size){
  if(!"OWNER".equals(actors.requireMini().actingRole()))throw new ViException(ViErrorCode.VI_FORBIDDEN);
  if(page<1||size<1||size>100)throw new ViException(ViErrorCode.VI_REQUEST_INVALID);
  // C03已明确每站独立额度，尚无权威区域编码映射，不能静默忽略区域筛选。
  if(area!=null&&!area.isBlank())throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"请先选择日期；服务范围在填写地址后核实");
  LocalDate day;
  try {day=date==null?LocalDate.now(ZONE):LocalDate.parse(date);if(date!=null&&!day.toString().equals(date))throw new IllegalArgumentException();}
  catch(RuntimeException e){throw new ViException(ViErrorCode.VI_REQUEST_INVALID,"日期须为YYYY-MM-DD");}
  if(day.isBefore(LocalDate.now(ZONE)))return new Page(200,"操作成功",List.of(),0);
  var candidates=mapper.candidates(day.atStartOfDay(ZONE).toInstant(),day.plusDays(1).atStartOfDay(ZONE).toInstant());
  var format=DateTimeFormatter.ofPattern("MM月dd日 HH:mm").withZone(ZONE);
  // ID仅代表该时间区间的当前候选锚点，不授予所属站点名额；B02须按地址重新匹配。
  var rows=candidates.stream().skip((long)(page-1)*size).limit(size).map(q->new Slot(q.getId().toString(),q.getStartsAt().toString(),q.getEndsAt().toString(),format.format(q.getStartsAt())+"—"+format.format(q.getEndsAt()),true,"CANDIDATE")).toList();
  return new Page(200,"操作成功",rows,candidates.size());
 }
}
