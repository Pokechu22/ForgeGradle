/*
 * A Gradle plugin for the creation of Minecraft mods and MinecraftForge plugins.
 * Copyright (C) 2013-2018 Minecraft Forge
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package net.minecraftforge.gradle.tasks;

import groovy.lang.Closure;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.gradle.testsupport.TaskTest;
import net.minecraftforge.gradle.testsupport.TestResource;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.*;
import java.util.zip.*;

public class TestMergeJars extends TaskTest<MergeJars>
{
    private static Closure<File> fileClosure(File f) {
        return new Closure<File>(null)
        {
            @Override
            public File call()
            {
                return f;
            }
        };
    }

    private static String zipName(Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    @Test
    public void testResourceZips() throws IOException
    {
        test(TestResource.MERGE_A_ZIP, TestResource.MERGE_B_ZIP, TestResource.MERGE_EXPECTED_ZIP, "resource");
    }

    @Test
    public void testJars() throws IOException
    {
        test(TestResource.MERGE_CLIENT_JAR, TestResource.MERGE_SERVER_JAR, TestResource.MERGE_EXPECTED_JAR, "code");
    }

    private void test(TestResource client, TestResource server, TestResource expectedRes, String name) throws IOException
    {
        File a = client.getFile(temporaryFolder);
        File b = server.getFile(temporaryFolder);
        File out = temporaryFolder.newFile(name + "-out.jar");
        File expected = expectedRes.getFile(temporaryFolder);

        MergeJars mergeJars = getTask(MergeJars.class);
        mergeJars.setClient(fileClosure(a));
        mergeJars.setServer(fileClosure(b));
        mergeJars.setOutJar(out);
        mergeJars.doTask();

        try (JarFile expectedJar = new JarFile(expected);
             JarFile outJar = new JarFile(out))
        {
            Set<String> expectedSet = expectedJar.stream().filter(TestMergeJars::isFile).map(ZipEntry::getName).collect(Collectors.toSet());
            // Side/SideOnly should always be in the output jar even if not in either input jar
            expectedSet.add(zipName(Side.class));
            expectedSet.add(zipName(SideOnly.class));
            Set<String> outSet = outJar.stream().filter(TestMergeJars::isFile).map(ZipEntry::getName).collect(Collectors.toSet());
            Assert.assertEquals("Entries in expected merged jar should match output merged jar", expectedSet, outSet);

            // since we're assuming we don't merge directory entries, there should be none at all
            // we should either have all of them or none
            Assert.assertEquals(0, outJar.stream().filter(it -> !isFile(it)).count());

            // Check tha the contents match
            expectedJar.stream().filter(TestMergeJars::isFile).forEach(expectedEntry -> {
                JarEntry outEntry = outJar.getJarEntry(expectedEntry.getName());
                Assert.assertEquals("Expected " + expectedEntry.getName() + " entries to match",
                        Long.toHexString(expectedEntry.getCrc()), Long.toHexString(outEntry.getCrc()));
            });
        }
    }

    /**
     * directory entries are not required by the zip spec so it's fine if those aren't matching
     */
    private static boolean isFile(JarEntry jarEntry)
    {
        return !jarEntry.getName().endsWith("/");
    }
}
