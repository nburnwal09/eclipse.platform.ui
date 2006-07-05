/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorProxyAdapter;

public class RefactoringHistorySerializationTests extends TestCase {

	private void compareReadHistory(RefactoringDescriptor[] descriptors, int flags, String xml, boolean ioException) throws CoreException {
		List list= new ArrayList();
		for (int index= 0; index < descriptors.length; index++) {
			list.add(new RefactoringDescriptorProxyAdapter(descriptors[index]));
		}
		try {
			ByteArrayInputStream stream= null;
			if (ioException) {
				stream= new ByteArrayInputStream(xml.getBytes("utf-8")) {

					public int read(byte[] b) throws IOException {
						throw new IOException();
					}
				};
			} else
				stream= new ByteArrayInputStream(xml.getBytes("utf-8"));
			RefactoringHistory result= RefactoringCore.getHistoryService().readRefactoringHistory(stream, flags);
			RefactoringDescriptorProxy[] actualProxies= result.getDescriptors();
			RefactoringDescriptorProxy[] expectedProxies= (RefactoringDescriptorProxy[]) list.toArray(new RefactoringDescriptorProxy[list.size()]);
			assertEquals("The number of refactoring descriptors is incorrect.", expectedProxies.length, actualProxies.length);
			for (int index= 0; index < expectedProxies.length; index++) {
				RefactoringDescriptor expectedDescriptor= expectedProxies[index].requestDescriptor(null);
				assertNotNull("Expected refactoring descriptor cannot be resolved.", expectedDescriptor);
				RefactoringDescriptor actualDescriptor= actualProxies[index].requestDescriptor(null);
				assertNotNull("Actual refactoring descriptor cannot be resolved.", actualDescriptor);
				assertEquals("Expected refactoring descriptor is not equal to actual one:", expectedDescriptor.toString(), actualDescriptor.toString());
			}
		} catch (UnsupportedEncodingException exception) {
			assertFalse("Unsupported encoding for ByteArrayOutputStream.", false);
		}
	}

	private void compareWrittenDescriptor(RefactoringSessionDescriptor descriptor, boolean time, String xml) throws CoreException {
		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		RefactoringCore.getHistoryService().writeRefactoringSession(descriptor, stream, time);
		try {
			assertEquals("The refactoring descriptor has not been correctly serialized:", xml, stream.toString("utf-8"));
		} catch (UnsupportedEncodingException exception) {
			assertFalse("Unsupported encoding for ByteArrayOutputStream.", false);
		}
	}

	public void testReadDescriptor0() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session comment=\"A mock comment\" version=\"1.0\">\r\n" + "<refactoring arg0=\"value0\" arg1=\"value1\" arg2=\"value2\" comment=\"A mock comment\" description=\"A mock refactoring\" flags=\"3\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.NONE;
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor("test0", "A mock refactoring", "A mock comment", RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value0");
		arguments.put("arg1", "value1");
		arguments.put("arg2", "value2");
		compareReadHistory(new RefactoringDescriptor[] { descriptor}, flags, xml, false);
	}

	public void testReadDescriptor1() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session comment=\"A mock comment\" version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" arg1=\"value 1\" arg2=\"value 2\" comment=\"A mock comment\" description=\"A mock refactoring\" flags=\"6\" id=\"org.eclipse.ltk.core.mock\" project=\"test1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.NONE;
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor("test1", "A mock refactoring", "A mock comment", RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value 0");
		arguments.put("arg1", "value 1");
		arguments.put("arg2", "value 2");
		compareReadHistory(new RefactoringDescriptor[] { descriptor}, flags, xml, false);
	}

	public void testReadDescriptor10() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, true);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for refactoring history io error:", IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getStatus().getCode());
		}
	}

	public void testReadDescriptor11() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refact oring arg0=\"value 0\" com ment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, false);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for refactoring history io error:", IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getStatus().getCode());
		}
	}

	public void testReadDescriptor12() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" com ment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, true);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for refactoring history io error:", IRefactoringCoreStatusCodes.REFACTORING_HISTORY_IO_ERROR, exception.getStatus().getCode());
		}
	}

	public void testReadDescriptor2() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.NONE;
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value 0");
		compareReadHistory(new RefactoringDescriptor[] { descriptor}, flags, xml, false);
	}

	public void testReadDescriptor3() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.NONE;
		MockRefactoringDescriptor first= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		MockRefactoringDescriptor second= new MockRefactoringDescriptor(null, "Another mock refactoring", "No comment", RefactoringDescriptor.BREAKING_CHANGE);
		Map arguments= first.getArguments();
		arguments.put("arg0", "value 0");
		arguments= second.getArguments();
		arguments.put("arg1", "value 1");
		compareReadHistory(new RefactoringDescriptor[] { first, second}, flags, xml, false);
	}

	public void testReadDescriptor4() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.NONE;
		MockRefactoringDescriptor first= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		MockRefactoringDescriptor second= new MockRefactoringDescriptor(null, "Another mock refactoring", "No comment", RefactoringDescriptor.BREAKING_CHANGE);
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= first.getArguments();
		arguments.put("arg0", "value 0");
		arguments= second.getArguments();
		arguments.put("arg1", "value 1");
		arguments.put("version", "1.0");
		arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		compareReadHistory(new RefactoringDescriptor[] { first, second, third}, flags, xml, false);
	}

	public void testReadDescriptor5() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.BREAKING_CHANGE;
		MockRefactoringDescriptor second= new MockRefactoringDescriptor(null, "Another mock refactoring", "No comment", RefactoringDescriptor.BREAKING_CHANGE);
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= second.getArguments();
		arguments.put("arg1", "value 1");
		arguments.put("version", "1.0");
		arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		compareReadHistory(new RefactoringDescriptor[] { second, third}, flags, xml, false);
	}

	public void testReadDescriptor6() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, false);
	}

	public void testReadDescriptor7() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"3.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, false);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for unsupported refactoring history version exception:", IRefactoringCoreStatusCodes.UNSUPPORTED_REFACTORING_HISTORY_VERSION, exception.getStatus().getCode());
		}
	}

	public void testReadDescriptor8() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session>\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, false);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for missing refactoring history version exception:", IRefactoringCoreStatusCodes.MISSING_REFACTORING_HISTORY_VERSION, exception.getStatus().getCode());
		}
	}

	public void testReadDescriptor9() throws Exception {
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<error version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</error>\r\n" + "";
		int flags= RefactoringDescriptor.MULTI_CHANGE;
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		try {
			compareReadHistory(new RefactoringDescriptor[] { third}, flags, xml, false);
		} catch (CoreException exception) {
			assertEquals("Wrong status code for refactoring history format exception:", IRefactoringCoreStatusCodes.REFACTORING_HISTORY_FORMAT_ERROR, exception.getStatus().getCode());
		}
	}

	public void testWriteDescriptor0() throws Exception {
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor("test0", "A mock refactoring", "A mock comment", RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value0");
		arguments.put("arg1", "value1");
		arguments.put("arg2", "value2");
		String version= "1.0";
		String comment= "A mock comment";
		RefactoringSessionDescriptor session= new RefactoringSessionDescriptor(new RefactoringDescriptor[] { descriptor}, version, comment);
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session comment=\"A mock comment\" version=\"1.0\">\r\n" + "<refactoring arg0=\"value0\" arg1=\"value1\" arg2=\"value2\" comment=\"A mock comment\" description=\"A mock refactoring\" flags=\"3\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\"/>\r\n" + "</session>\r\n" + "";
		compareWrittenDescriptor(session, true, xml);
	}

	public void testWriteDescriptor1() throws Exception {
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor("test1", "A mock refactoring", "A mock comment", RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value 0");
		arguments.put("arg1", "value 1");
		arguments.put("arg2", "value 2");
		String version= "2.0";
		String comment= "A mock comment";
		RefactoringSessionDescriptor session= new RefactoringSessionDescriptor(new RefactoringDescriptor[] { descriptor}, version, comment);
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session comment=\"A mock comment\" version=\"2.0\">\r\n" + "<refactoring arg0=\"value 0\" arg1=\"value 1\" arg2=\"value 2\" comment=\"A mock comment\" description=\"A mock refactoring\" flags=\"6\" id=\"org.eclipse.ltk.core.mock\" project=\"test1\"/>\r\n" + "</session>\r\n" + "";
		compareWrittenDescriptor(session, true, xml);
	}

	public void testWriteDescriptor2() throws Exception {
		MockRefactoringDescriptor descriptor= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		Map arguments= descriptor.getArguments();
		arguments.put("arg0", "value 0");
		String version= "2.0";
		String comment= null;
		RefactoringSessionDescriptor session= new RefactoringSessionDescriptor(new RefactoringDescriptor[] { descriptor}, version, comment);
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"2.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "</session>\r\n" + "";
		compareWrittenDescriptor(session, true, xml);
	}

	public void testWriteDescriptor3() throws Exception {
		MockRefactoringDescriptor first= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		MockRefactoringDescriptor second= new MockRefactoringDescriptor(null, "Another mock refactoring", "No comment", RefactoringDescriptor.BREAKING_CHANGE);
		Map arguments= first.getArguments();
		arguments.put("arg0", "value 0");
		arguments= second.getArguments();
		arguments.put("arg1", "value 1");
		String version= "1.0";
		String comment= null;
		RefactoringSessionDescriptor session= new RefactoringSessionDescriptor(new RefactoringDescriptor[] { first, second}, version, comment);
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"1.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "</session>\r\n" + "";
		compareWrittenDescriptor(session, false, xml);
	}

	public void testWriteDescriptor4() throws Exception {
		MockRefactoringDescriptor first= new MockRefactoringDescriptor(null, "A mock refactoring", "A mock comment", RefactoringDescriptor.NONE);
		MockRefactoringDescriptor second= new MockRefactoringDescriptor(null, "Another mock refactoring", "No comment", RefactoringDescriptor.BREAKING_CHANGE);
		MockRefactoringDescriptor third= new MockRefactoringDescriptor("test0", "Yet another mock refactoring", null, RefactoringDescriptor.BREAKING_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		Map arguments= first.getArguments();
		arguments.put("arg0", "value 0");
		arguments= second.getArguments();
		arguments.put("arg1", "value 1");
		arguments.put("version", "1.0");
		arguments= third.getArguments();
		arguments.put("arg2", "value 2");
		arguments.put("version", "1.1");
		String version= "3.0";
		String comment= null;
		RefactoringSessionDescriptor session= new RefactoringSessionDescriptor(new RefactoringDescriptor[] { first, second, third}, version, comment);
		String xml= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" + "<session version=\"3.0\">\r\n" + "<refactoring arg0=\"value 0\" comment=\"A mock comment\" description=\"A mock refactoring\" id=\"org.eclipse.ltk.core.mock\"/>\r\n" + "<refactoring arg1=\"value 1\" comment=\"No comment\" description=\"Another mock refactoring\" flags=\"1\" id=\"org.eclipse.ltk.core.mock\" version=\"1.0\"/>\r\n" + "<refactoring arg2=\"value 2\" description=\"Yet another mock refactoring\" flags=\"5\" id=\"org.eclipse.ltk.core.mock\" project=\"test0\" version=\"1.1\"/>\r\n" + "</session>\r\n" + "";
		compareWrittenDescriptor(session, true, xml);
	}
}