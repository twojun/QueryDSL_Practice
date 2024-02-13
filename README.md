# Chapter 1. QueryDSL 프로젝트 환경 설정 </br>

## 1-1. Spring Boot 3.x 이상</br>
https://velog.io/@juhyeon1114/Spring-QueryDsl-gradle-%EC%84%A4%EC%A0%95-Spring-boot-3.0-%EC%9D%B4%EC%83%81</br></br>

build and run using : Gradle</br>
test : IntelliJ IDEA </br></br></br></br></br>




## 1-2. 관련 라이브러리 확인</br>
querydsl-apt : 엔티티에 대한 Q파일 생성</br>
querydsl-jpa : 실제 쿼리를 작성하기 위한 라이브러리 </br></br></br></br>



## 1-3. Spring boot, JPA, DB 기타 설정 </br>
(1) Test에서 @Transactional : 기본 설정은 롤백, @Commit 어노테이션을 적용해서 테스트 케이스에서도 커밋 가능 </br></br>
(2) p6spy : implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0' (스프링 부트 3.0 이상) </br>
- 운영이 중요한 환경에서는 성능 이슈가 발생할 수 있다. 부하 테스트 이후 사용해보는 것이 좋다.</br></br></br></br></br></br></br>






# Chapter 2. QueryDSL Basic </br>
## 2-1. JPQL, QueryDSL 차이 확인하기 </br>
(1) JPQL, Querydsl 모두 데이터베이스의 데이터를 조작하기 위한 도구들이지만 아래와 같은 차이점이 존재한다.</br></br>

(2) 문법 및 표현 방식</br>
- JPQL : 엔티티, 필드 및 쿼리들을 문자열 형태로 작성</br>
- QueryDSL : 자바 코드 자체가 쿼리를 표현하고 있으며 엔티티, 필드들이 모두 자바 계열의 클래스로 표현된다.</br></br>

(3) 오류 검증 시점</br>
- JPQL : 문자열 형태로 쿼리, 엔티티, 필드를 모두 작성하기 때문에 Type-safed가 보장되지 않으며 오류를 런타임 시점에서 잡을 수 있다.</br>
- QueryDSL :  모든 영역들이 자바 코드로 작성되기 때문에 Type-Safed가 보장되며 오류가 발생하더라도 컴파일 시점에서 잡을 수 있고 IDE가 지원하는 타입 검증으로 미리 오류를 체크할 수 있다.</br></br>

(4) 복잡한 비즈니스에서 작성해야 하는 동적 쿼리, 통계성 쿼리 작성에서의 이점 </br>
- JPQL : 동적 쿼리 작성 시 문자열 조합으로 작성해야 하기 때문에 문자열의 지나친 들여쓰기, 조건문 사용 등으로 인해 쿼리 작성에 어려움이 있으며 가독성이 저하된다.</br>
- QueryDSL : 복잡한 동적 쿼리를 자바 코드로 작성하기 때문에 가독성이 높고 작성하기 수월한 편이다.</br></br></br></br></br>




## 2-2. 기본 Q-Type 활용 </br>
(1) QType : Querydsl에서 엔티티를 조회하고 조작하기 위해 사용되는 타입으로 엔티티의 필드와 테이블을 대표하는 Q-Type을 제공하고 있다.</br>
- 사용 시 직접 선언하지 않고, QMember.member처럼 먼저 작성 후 static import하여 사용하면 간결하게 사용할 수 있다.</br></br>

(2) 같은 테이블을 조인해야 할 상황이 있을 때 alias를 Q-Type을 선언해서 따로 사용할 수도 있다.</br>
- QMember m1 = new QMember("m1") 형식 </br></br></br></br></br>




## 2-3. 검색 조건 쿼리 </br>
(1) JPQL이 제공하는 모든 검색 조건을 제공한다.</br>
member.username.eq("member1") // username = 'member1'</br>
 member.username.ne("member1") //username != 'member1'</br>
 member.username.eq("member1").not() // username != 'member1'</br></br>
member.username.isNotNull() //이름이 is not null</br></br>
 member.age.in(10, 20) // age in (10,20)</br>
 member.age.notIn(10, 20) // age not in (10, 20)</br>
 member.age.between(10,30) //between 10, 30</br></br>
 member.age.goe(30) // age >= 30</br>
 member.age.gt(30) // age > 30</br>
 member.age.loe(30) // age <= 30</br>
 member.age.lt(30) // age < 30</br></br>
member.username.like("member%") //like 검색 </br>
member.username.contains("member") // like ‘%member%’ 검색 </br>
member.username.startsWith("member") //like ‘member%’ 검색 ...</br></br>

(2) AND 조건을 파라미터(,)로 넘김으로써 한 번에 처리 가능</br>
- where()에 파라미터로 ,(comma)를 통해 검색 조건을 추가하면 AND을 명시한 것과 동일하다 </br></br></br></br></br>




## 2-4. 결과 조회 </br>
(1) fetch() : 리스트 조회, 데이터가 없으면 빈 리스트 반환</br></br>

(2) fetchOne() : 단 건 조회 </br>
- 결과가 없으면 null</br>
- 결과가 둘 이상이면 com.querydsl.core.NonUniqueResultException 예외 발생</br></br>

(3) fetchFirst() : limit(1).fetchOne()</br>
(4) fetchResults() : 페이징 정보를 포함, total count 쿼리가 추가로 실행됨 </br>
(5) fetchCount() : select에서 count 쿼리로 변경해서 count 수를 조회 </br></br></br></br></br>




## 2-5. 정렬  </br>
(1) desc(), asc() : 내림차순/오름차순 </br>
(2) nullLast(), nullFirst() : null 데이터에 순서를 부여한다. </br> </br> </br> </br></br>




## 2-6. 페이징 처리 </br>
(1) fetchResults() 사용 시 select count 쿼리가 발생되니 주의한다.queryResults </br>
- 실무에서 페이징 쿼리 작성 시 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만 count 쿼리의 경우 조인이 필요하지 않은 경우도 있다.
  이렇게 자동회된 count 쿼리는 원본 쿼리와 같이 join을 해버리는 경우가 있기 때문에 성능상 최적화가 필요할 경우 count 쿼리는 따로 작성하는 것이 좋다. </br> </br> </br> </br></br>




## 2-7. 집합 </br>
(1) 튜플이 사용되는 이유? </br>
- 단일 타입이 아닌 여러 개의 데이터 타입이 들어오기 때문에 튜플이 사용된다. </br> 
- 실무에서는 튜플은 많이 쓰지 않고 DTO로 뽑아오는 방법을 많이 사용한다.  </br> </br>

(2) JPQL이 제공하는 집합 함수를 모두 제공한다</br>
- 프로젝션 결과 반환 시 tuple이 사용되는 이유를 확인해보자 </br></br>

(3) groupBy()를 통해 그룹화, 그룹화된 결과를 제한하기 위해 having() 사용 가능 </br></br></br></br></br>




## 2-8. join - 일반 join </br>
(1) 일반 join</br>
- join의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고 두 번째 파라미터에 별칭으로 사용할 Q타입을 지정한다.</br>
- join(조인 대상, 별칭으로 사용할 QType)</br></br>

(2) innerJoin() = join() > 내부 조인</br>
(3) leftJoin() left 외부 조인 (left outer join)</br>
(4) rightJoin() right 외부 조인(right outer join)</br>
(5) fetchJoin() = JPA에서 성능 최적화를 위한 fetch 조인</br></br>


(6) 세타 조인 - 연관관계가 없는 필드들을 조인하는 것</br>
- 일반적으로 세타 조인은 두 테이블을 조인할 때 일반적인 조인 조건을 명시하지 않고 임의의 조건(where절)을 명시해 튜플을 끌고오는 조인이다</br>
- jpa에서는 from 절에 여러 엔티티를 조인해서 세타 조인이 가능하다.</br></br></br></br></br>




## 2-9. 조인 대상 필터링 - on절 </br>
(1) jpql : select m, t from Member m left join m.team t on t.name = "teamA"</br>
(2) sql : select m.*, t.* from member m left join team t on m.team_id = t.id and t.name = 'teamA';</br></br>

(3) left join</br>
- 위의 쿼리에서 member 엔티티를 중심으로 team 엔티티와 left join을 수행한다. member 로우들은 모두 끌고 오고,</br>
  on 조건절을 대상으로 조건을 만족하는 team 로우들을 가져오게 된다. 조건을 만족하는 team 로우가 없더라도 member 로우는 모두 끌고 온다.</br></br>

(4) right join</br>
- 위의 쿼리에서 member 엔티티를 중심으로 team 엔티티와 right join을 수행한다. team 로우들은 모두 끌고 오고 </br>
  on 조건절을 대상으로 조건을 만족하는 member 로우들을 가져오게 된다. 조건을 만족하는 team 로우가 없더라도 team 로우는 모두 끌고 온다.</br></br>

(5) inner join인 경우 Join on 조건으로 걸러내나, where로 걸러내나 결과값은 동일하다 </br>
따라서 inner join인 경우 where로 풀 수 있으면 풀고 Left/right(outer) join이 필요한 경우에만 on 조건절을 활용한다</br></br></br></br></br>




## 2-10. fetch join </br>
(1) fetch join은 JPA에서 특정 엔티티 조회 시 연관된 엔티티를 한 번에 불러옴으로써 성능 최적화를 위해 지원되는 기능 중 하나이며
표준 SQL 문법은 아니다.</br></br>

(2) join(), leftJoin(), rightJoin() 이후에 fetchJoin() 메서드를 추가해주면 된다.</br></br></br></br></br>




## 2-11. 서브 쿼리 </br>
(1) 서브 쿼리 </br>
- JPAExpressions를 써야 한다. - static import 사용 가능</br>
- alias를 추가로 생성해 줘야 한다 </br></br>

(2) from 절의 서브 쿼리 한계</br>
- jpa, jpql 서브 쿼리 한계점으로 from 절의 서브쿼리는 지원되지 않는다.</br>
- 하이버네이트를 사용하면 select절의 서브쿼리는 지원하고 있어서 select절의 서브쿼리는 하이버네이트를 쓰면 사용할 수 있다.</br>
- 이에 대한 해결은, 서브쿼리는 웬만하면 join으로 변경 가능하다. 따라서 join으로 해결하자(물론 안 되는 경우도 있다.)</br>
- 애플리케이션에서 쿼리를 2번 분리해서 실행하거나 nativeSQL를 사용한다(JPQL의 한계점)</br></br></br></br></br>



## 2-12. 조건 case식, CaseBuilder</br>
(1) 조건식이 필요할 수도 있겠지만 데이터베이스는 순수하게 나열된 로우들을 순수히 넘기는 역할만 해야 한다.</br></br>

(2) 로우들이 특정 조건에 맞게 변경되어야 한다면 이 부분은 비즈니스 로직이나 , 애플리케이션 레벨, 프레젠테이션 레벨에서 처리하도록 한다 </br></br></br></br></br>



## 2-13. 상수, 문자 더하기</br>
(1) 문자 더하기 : concat</br>
(2) 문자가 아닌 다른 타입들은 stringVale() 메서드를 통해 문자로 변환할 수 있다.</br>
(3) 해당 메서드는 Enum Type을 주로 처리할 때 많이 사용되는 방법이다. </br></br></br></br></br></br></br>






# Chapter 3. QueryDSL Advanced </br>
## 3-1. 프로젝션 결과 반환 : 정의와 특징</br>
(1) 프로젝션이란 select 쿼리에 지정된 대상을 말한다. </br></br>

(2) 프로젝션 대상이 하나인 경우 타입을 명확하게 지정할 수 있다.</br></br>

(3) 프로젝션 대상이 둘 이상이면 tuple이나 DTO로 조회해야 한다.</br>
- tuple은 여러 개의 데이터 타입을 조회하기 위해 querydsl이 구현한 데이터 타입 </br>
- tuple은 최소한 Repository 영역에서만 사용한다. Service, Controller 계층까지 넘어가는 건 좋은 설계는 아니다. </br>
- Repository 영역을 벗어난 곳이라면, DTO로 변환해서 반환하는 것이 권장된다. </br>
- 구현 기술을 다른 계층에서 알게 되는 것은 좋은 설계가 아님. </br> </br> </br> </br></br>



## 3-2. 프로젝션 결과 반환 : DTO(Data Transfer Object)로 조회  </br>
