package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before_TestData_Setting() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void start_JPQLTest() {
        Member findMember = em.createQuery("select m " +
                                                    "from Member m " +
                                                    "where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void start_Querydsl_Test() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test     /*  검색 조건 쿼리  */
    public void search_Condition_Query_Test() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test   /* 검색 조건 쿼리 : where() parameter 연결로 and 처리 */
    public void search_Condition_Where_Query_Test() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test   /* 결과 조회 */
    public void result_Fetch_Query_Test() {
//        List<Member> fetch = queryFactory
//                .select(member)
//                .from(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .select(member)
//                .from(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .select(member)
//                .from(member)
//                .fetchFirst();
//
//        QueryResults<Member> results = queryFactory
//                .select(member)
//                .from(member)
//                .fetchResults();
//
//        results.getTotal();
//        List<Member> content = results.getResults();

        long total = queryFactory
                .select(member)
                .from(member)
                .fetchCount();
    }


    @Test
    /* 정렬
     * 회원 나이 desc, 이름 asc
     * 단 2에서 회원 이름이 없으면 마지막에 출력
     */
    public void sort_Query_Test() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    /**
     * 페이징 처리 : 조회 건수 제한
     */
    @Test
    public void paging1_Query_Test() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .orderBy(member.username.desc())
                .offset(1)  // Index start : 0
                .limit(2)
                .fetch();

        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    /**
     * 페이징 처리 : 전체 조회 건수 필요
     */
    @Test
    public void paging2_Query_Test() {
        QueryResults<Member> queryResults = queryFactory
                .select(member)
                .from(member)
                .orderBy(member.username.desc())
                .offset(1)  // Index start : 0
                .limit(2)
                .fetchResults();

        Assertions.assertThat(queryResults.getTotal()).isEqualTo(4);
        Assertions.assertThat(queryResults.getLimit()).isEqualTo(2);
        Assertions.assertThat(queryResults.getOffset()).isEqualTo(1);
        Assertions.assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * 집합 함수 : sum, count, avg, max, min
     */
    @Test
    public void aggregation_Query_Test() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * group by : 팀의 이름, 각 팀의 평균 연령
     */
    @Test
    public void groupBy_Query_Test() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 일반 join : 팀 A에 소속된 모든 회원
     */
    @Test
    public void general_Join_Query_Test() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .innerJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인 (연관관계가 없는 필드로 조인) : 조인 조건이 주어지지 않은 일반적 조인에서 카르테시안 곱의 원리로 세타 조인 발생할 수 있음
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_Join_Query_Test() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * on절을 활용한 조인 : 회원 모두 조회, 팀 이름이 teamA인 팀만 조인
     */
    @Test
    public void onClause_Filter_Join_Query_Test() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 조인
     */
    @Test
    public void no_Relation_Join_Query_Test()  {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .rightJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     *  fetch join 미적용
     */
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void no_Fetch_Join_Query_Test()  {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isloaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        Assertions.assertThat(isloaded).as("Fetch join 미적용").isFalse();
    }

    /**
     *  fetch join 적용
     */
    @Test
    public void use_Fetch_Join_Query_Test()  {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isloaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        Assertions.assertThat(isloaded).as("Fetch join 미적용").isTrue();
    }

    /**
     *  서브 쿼리 : 나이가 가장 많은 회원
     */
    @Test
    public void subQuery_Test() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result)
                .extracting("age")
                .containsExactly(40);
    }

    /**
     *  서브 쿼리 : 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQuery_Goe_Test() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result)
                .extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 조건식 case
     */
    @Test
    public void basic_Case_Query_Test() {
        List<String> result = queryFactory
                .select(
                member.age
                        .when(10).then("10")
                        .when(20).then("20")
                        .otherwise("etc")
        )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 조건식 case
     */
    @Test
    public void complex_Case_Query_Test()  {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 10)).then("0~10")
                        .when(member.age.between(21, 30)).then("21~30")
                        .otherwise("etc"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 상수
     */
    @Test
    public void constant_Query_Test()  {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 문자 더하기 _ concat
     */
    @Test
    public void string_Concat_Query_Test()  {
        String str = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("str = " + str);
        Assertions.assertThat(str).isEqualTo("member1_10");
    }

    /**
     * 프로젝션 대상이 하나
     */
    @Test
    public void simple_Projection_Query_Test()  {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 프로젝션 대상 : tuple
     */
    @Test
    public void tuple_Projection_Query_Test()  {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        }
    }

    /**
     * 프로젝션 대상 조회 : DTO : JPQL
     */
    @Test
    public void find_Dto_ByJPQL_Query_Test() throws Exception {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto" +
                        "(m.username," +
                        " m.age) " +
                        "from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 프로젝션 대상 조회 : querydsl : setter, constructor, property 접근
     */
    @Test
    public void find_Dto_By_Querydsl_Setter_Query_Test() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    
    @Test
    public void find_Dto_By_Querydsl_Constructor_Query_Test() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_Dto_By_Querydsl_Property_Query_Test() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void find_UserDto_By_Querydsl_Property_Query_Test() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void find_UserDto_By_Querydsl_Property_Query_Test2() throws Exception {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    /**
     * 프로젝션 : QueryProjection
     */

    @Test
    public void find_By_Dto_QueryProjection_Query_Test() throws Exception {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 동적 쿼리 : BooleanBuilder
     */
    @Test
    public void booleanBuilder_Dynamic_Query_Test() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    /**
     * 동적 쿼리 : whereParam
     */
    @Test
    public void whereParam_Dynamic_Query_Test() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .select(member)
                .from(member)
                .where(builder)
                .fetch();
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        boolean isPrime = true;
        return queryFactory
                .select(member)
                .from(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }


    /**
     * 벌크 연산
     */
    @Test
    public void bulk_Update_Query_Test() throws Exception {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

    }
}






