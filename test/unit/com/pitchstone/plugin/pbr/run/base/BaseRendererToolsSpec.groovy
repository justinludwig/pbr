package com.pitchstone.plugin.pbr.run.base

import com.pitchstone.plugin.pbr.load.base.BaseLoader
import com.pitchstone.plugin.pbr.load.base.BaseModule
import spock.lang.Specification

class BaseRendererToolsSpec extends Specification {

    def tools = new BaseRendererTools()

    def "null attrs is blank"() {
        expect: tools.attrs(null) == ''
    }

    def "empty attrs is blank"() {
        expect: tools.attrs([:]) == ''
    }

    def "one simple attrs"() {
        expect: tools.attrs(foo: 'bar') == ' foo="bar"'
    }

    def "three simple attrs"() {
        expect: tools.attrs(foo: 'x', bar: 'y', baz: 'z') == ' foo="x" bar="y" baz="z"'
    }

    def "illegal attrs name chars are stripped"() {
        expect: tools.attrs(/"foo -_.\u0000baz09'/: 'bar') == ' foo-_.baz09="bar"'
    }

    def "unsafe attr value chars are escaped"() {
        expect: tools.attrs(foo: /<"x&y's">/) == ' foo="&lt;&quot;x&amp;y\'s&quot;&gt;"'
    }

    def "null attrs key is skipped"() {
        expect: tools.attrs((null): 'bar') == ''
    }

    def "null attrs value is skipped"() {
        expect: tools.attrs(foo: null) == ''
    }

    def "empty string attrs key is skipped"() {
        expect: tools.attrs('': 'bar') == ''
    }

    def "empty string attrs value is skipped"() {
        expect: tools.attrs(foo: '') == ''
    }

    def "boolean attrs key used for attr name"() {
        expect: tools.attrs((true): 'bar') == ' true="bar"'
    }

    def "boolean true attrs value rendered"() {
        expect: tools.attrs(foo: true) == ' foo'
    }

    def "boolean false attrs value skipped"() {
        expect: tools.attrs(foo: false) == ''
    }

    def "number attrs key used for attr name"() {
        expect: tools.attrs((99): 'bar') == ' 99="bar"'
    }

    def "non-zero attrs value rendered"() {
        expect: tools.attrs(foo: 99) == ' foo="99"'
    }

    def "zero attrs value rendered"() {
        expect: tools.attrs(foo: 0) == ' foo="0"'
    }

    def "empty list attrs key is skipped"() {
        expect: tools.attrs(([]): 'bar') == ''
    }

    def "empty list attrs value is skipped"() {
        expect: tools.attrs(foo: []) == ''
    }

    def "one-entry list attrs key used for attr name"() {
        expect: tools.attrs((['foo']): 'bar') == ' foo="bar"'
    }

    def "one-entry list attrs value used for attr value"() {
        expect: tools.attrs(foo: ['bar']) == ' foo="bar"'
    }

    def "entire three-entry list attrs key used for attr name"() {
        expect: tools.attrs((['foo', 'goo', 'who']): 'bar') == ' foogoowho="bar"'
    }

    def "only first three-entry list attrs value used for attr value"() {
        expect: tools.attrs(foo: ['bar', 'bass', 'bat']) == ' foo="bar"'
    }

    def "null or empty list attrs value skipped for attr value"() {
        expect: tools.attrs(foo: [null, '', 'bar']) == ' foo="bar"'
    }

    def "nested list attrs value flattened before using as attr value"() {
        expect: tools.attrs(foo: [[null, '', 'bar'], 'baz']) == ' foo="bar"'
    }

    def "simple string class attrs value"() {
        expect: tools.attrs(class: 'foo') == ' class="foo"'
    }

    def "empty-list class attrs skipped"() {
        expect: tools.attrs(class: []) == ''
    }

    def "one-entry-list class attrs value used for attr value"() {
        expect: tools.attrs(class: ['foo']) == ' class="foo"'
    }

    def "entire three-entry-list class attrs value used for attr value"() {
        expect: tools.attrs(class: ['foo', 'bar', 'baz']) == ' class="foo bar baz"'
    }

    def "null or empty list class attrs values skipped for attr value"() {
        expect: tools.attrs(class: [null, '', 'foo']) == ' class="foo"'
    }

    def "nested-list class attrs value flattened before using as attr value"() {
        expect: tools.attrs(class: [[null, '', 'foo'], 'bar']) == ' class="foo bar"'
    }

    def "simple string style attrs value"() {
        expect: tools.attrs(style: 'foo') == ' style="foo"'
    }

    def "empty-list style attrs skipped"() {
        expect: tools.attrs(style: []) == ''
    }

    def "one-entry-list style attrs value used for attr value"() {
        expect: tools.attrs(style: ['foo']) == ' style="foo"'
    }

    def "entire three-entry-list style attrs value used for attr value"() {
        expect: tools.attrs(style: ['foo', 'bar', 'baz']) == ' style="foo;bar;baz"'
    }

    def "null or empty list style attrs values skipped for attr value"() {
        expect: tools.attrs(style: [null, '', 'foo']) == ' style="foo"'
    }

    def "nested-list style attrs value flattened before using as attr value"() {
        expect: tools.attrs(style: [[null, '', 'foo'], 'bar']) == ' style="foo;bar"'
    }

    def "empty-map style attrs skipped"() {
        expect: tools.attrs(style: [:]) == ''
    }

    def "one-entry-map style attrs value used for attr value"() {
        expect: tools.attrs(style: [foo:'x']) == ' style="foo:x"'
    }

    def "entire three-entry-map style attrs value used for attr value"() {
        expect: tools.attrs(style: [foo:1, bar:2, baz:3]) == ' style="foo:1;bar:2;baz:3"'
    }

    def "null or empty map style attrs keys skipped for attr value"() {
        expect: tools.attrs(style: [(null):1, '':2, foo:3]) == ' style="foo:3"'
    }

    def "null or empty map style attrs values skipped for attr value"() {
        expect: tools.attrs(style: [foo:null, bar:'', baz:'x']) == ' style="baz:x"'
    }

    def "zero style attrs values not skipped for attr value"() {
        expect: tools.attrs(style: [foo:0]) == ' style="foo:0"'
    }



    def "null element text is blank"() {
        expect: tools.text(null) == ''
    }

    def "empty element text is blank"() {
        expect: tools.text('') == ''
    }

    def "zero element text is zero string"() {
        expect: tools.text(0) == '0'
    }

    def "plain element text is echoed"() {
        expect: tools.text('foo') == 'foo'
    }

    def "angle bracket and ampersand element text is encoded"() {
        expect: tools.text('<&"\'>') == '&lt;&amp;"\'&gt;'
    }



    def "null attr name is blank"() {
        expect: tools.attrName(null) == ''
    }

    def "empty attr name is blank"() {
        expect: tools.attrName('') == ''
    }

    def "zero attr name is zero string"() {
        expect: tools.attrName(0) == '0'
    }

    def "alphanumeric attr name is echoed"() {
        expect: tools.attrName('_fOO-0.9:') == '_fOO-0.9:'
    }

    def "non-alphanumeric chars in attr name are removed"() {
        expect: tools.attrName('\u0000<&"\'> a!@#$%^&*') == 'a'
    }



    def "null attr value is blank"() {
        expect: tools.attrValue(null) == ''
    }

    def "empty attr value is blank"() {
        expect: tools.attrValue('') == ''
    }

    def "zero attr value is zero string"() {
        expect: tools.attrValue(0) == '0'
    }

    def "plain attr value is echoed"() {
        expect: tools.attrValue('foo') == 'foo'
    }

    def "angle bracket, ampersand, and double quote attr value is encoded"() {
        expect: tools.attrValue('<&"\'>') == '&lt;&amp;&quot;\'&gt;'
    }

}
