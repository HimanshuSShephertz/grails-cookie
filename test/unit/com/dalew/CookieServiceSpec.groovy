package com.dalew

import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@TestFor(CookieService)
class CookieServiceSpec extends Specification {
    @Shared
    HttpServletRequest request
    @Shared
    HttpServletResponse response
    @Shared
    CookieService cookieService

    def setupSpec() {
        CookieUtils.extendReqResp()
    }

    def setup() {
        response = new MockHttpServletResponse()
        request = new MockHttpServletRequest()
        def mockWebRequest = new GrailsWebRequest(request, response, new MockServletContext())
        WebUtils.storeGrailsWebRequest(mockWebRequest)
        cookieService = service
    }

    def cleanup() {
        WebUtils.clearGrailsWebRequest()
    }

    def "findCookie()"() {
        given:
        def cookie = new Cookie('some_cookie_name', 'cookie_value')
        request.cookies = [cookie]
        expect:
        cookieService.findCookie('some_cookie_name') == cookie
    }

    @Unroll
    def "getCookie() return cookie value and is case sensetive: #obj.class.simpleName #params.name #params.expectedValue"() {
        given:
        request.cookies = [new Cookie('cookie_name', 'some value')]
        expect:
        obj.getCookie(params.name) == params.expectedValue
        where:
        [obj, params] << [
                [cookieService, request], [
                [name: 'cookie_name', expectedValue: 'some value'],
                [name: 'CoOkIe_NaMe', expectedValue: null]
        ]].combinations()
    }

    @Unroll
    void "setCookie(): #name #value #maxAge #path #domain #secure #httpOnly"() {
        given:
        cookieService.setCookie(args)
        def cookie = response.cookies[0]
        expect:
        cookie.name == name
        cookie.value == value
        cookie.maxAge == maxAge
        cookie.path == path
        cookie.domain == domain
        cookie.secure == secure
        cookie.httpOnly == httpOnly
        cookie.version == 1
        where:
        args                                                                   | name          | value        | maxAge  | path    | domain         | secure | httpOnly
        ['cookie_name', 'cookie_val']                                          | 'cookie_name' | 'cookie_val' | 2592000 | '/'     | null           | false  | false
        ['cookie_name', 'cookie_val', 42]                                      | 'cookie_name' | 'cookie_val' | 42      | '/'     | null           | false  | false
        ['cookie_name', 'cookie_val', 42, '/path']                             | 'cookie_name' | 'cookie_val' | 42      | '/path' | null           | false  | false
        ['cookie_name', 'cookie_val', 42, '/path', '.example.com', true, true] | 'cookie_name' | 'cookie_val' | 42      | '/path' | '.example.com' | true   | true
    }

    @Unroll
    void "setCookie() named params: #name #value #maxAge #path #domain #secure #httpOnly"() {
        given:
        cookieService.setCookie(args)
        def cookie = response.cookies[0]
        expect:
        cookie.name == name
        cookie.value == value
        cookie.maxAge == maxAge
        cookie.path == path
        cookie.domain == domain
        cookie.secure == secure
        cookie.httpOnly == httpOnly
        cookie.version == 1
        where:
        args                                                                                                                        | name          | value        | maxAge  | path    | domain         | secure | httpOnly
        [name: 'cookie_name', value: 'cookie_val']                                                                                  | 'cookie_name' | 'cookie_val' | 2592000 | '/'     | null           | false  | false
        [name: 'cookie_name', value: 'cookie_val', maxAge: 42]                                                                      | 'cookie_name' | 'cookie_val' | 42      | '/'     | null           | false  | false
        [name: 'cookie_name', value: 'cookie_val', maxAge: 42, path: '/path']                                                       | 'cookie_name' | 'cookie_val' | 42      | '/path' | null           | false  | false
        [name: 'cookie_name', value: 'cookie_val', maxAge: 42, path: '/path', domain: '.example.com', secure: true, httpOnly: true] | 'cookie_name' | 'cookie_val' | 42      | '/path' | '.example.com' | true   | true
    }

    @Unroll
    def "deleteCookie() sets new cookie with same name but expired age: #name #path #domain"() {
        given:
        cookieService.deleteCookie(args)
        def cookie = response.cookies[0]
        expect:
        cookie.name == name
        cookie.value == null
        cookie.path == path
        cookie.domain == domain
        cookie.maxAge == 0
        cookie.version == 1
        where:
        args                                     | name          | path    | domain
        ['cookie_name']                          | 'cookie_name' | '/'     | null
        ['cookie_name', '/path']                 | 'cookie_name' | '/path' | null
        ['cookie_name', '/path', '.example.com'] | 'cookie_name' | '/path' | '.example.com'

    }

    @Unroll
    def "deleteCookie(Cookie) sets new cookie with same name but expired age: #name #path #domain"() {
        given:
        Cookie cookieToDelete = new Cookie(name, 'some_val')
        cookieToDelete.path = pathIncomming
        if (domain) {
            cookieToDelete.domain = domain
        }
        cookieService.deleteCookie(cookieToDelete)
        def cookie = response.cookies[0]
        expect:
        cookie.name == name
        cookie.value == null
        cookie.path == pathOucomming
        cookie.domain == domain
        cookie.maxAge == 0
        cookie.version == 1
        where:
        name          | pathIncomming | pathOucomming | domain
        'cookie_name' | null          | '/'           | null
        'cookie_name' | '/'           | '/'           | null
        'cookie_name' | '/path'       | '/path'       | null
        'cookie_name' | '/path'       | '/path'       | '.example.com'
    }
}
