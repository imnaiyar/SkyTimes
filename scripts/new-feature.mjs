#!/usr/bin/env node

// Scaffold a new feature module and register it in the build.
//
// Creates feature/<name>/ (module + placeholder screen) and updates the
// places that reference feature modules:
//
//   * settings.gradle.kts   -- include(":feature:<name>")
//   * app/build.gradle.kts  -- implementation(projects.feature.<name>)
//
// Usage:
//   node scripts/new-feature.mjs <name> [--title "Display Title"]
//
// Example:
//   node scripts/new-feature.mjs arena --title "Arena Battles"
//
// The name must be lowercase letters/digits (matches the existing feature module
// convention: home, quests, reminders, settings, vault) because it is used for
// the module path, the Kotlin package, the generated project accessor
// (projects.feature.<name>) and the PascalCase class names.

import { readFileSync, writeFileSync, mkdirSync, existsSync } from "node:fs";
import { dirname, join, relative } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const NAME_RE = /^[a-z][a-z0-9]*$/;

const FEATURE_BUILD_TEMPLATE = `plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
    }
}
`;

function screenTemplate(name, Name, title) {
  return `package com.imnaiyar.skytimes.${name}

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun ${Name}Screen() {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "${title}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
`;
}

function fail(msg) {
  console.error(`error: ${msg}`);
  process.exit(1);
}

function read(path) {
  if (!existsSync(path)) fail(`expected file not found: ${relative(ROOT, path)}`);
  // Split without leaving a phantom empty trailing element so the
  // join("\n") + "\n" round-trip keeps the file byte-identical.
  return readFileSync(path, "utf8").replace(/\n$/, "").split("\n");
}

function write(path, lines) {
  writeFileSync(path, lines.join("\n") + "\n");
}

function sortedInsert(lines, pattern, newLine, fileDesc) {
  // Insert newLine among the lines matching pattern, keeping them sorted and indented.
  const idxs = [];
  lines.forEach((line, i) => {
    if (pattern.test(line)) idxs.push(i);
  });
  if (!idxs.length) fail(`could not find block matching ${pattern} in ${fileDesc}`);
  if (lines.some((line) => line.trim() === newLine)) return; // already present
  const indent = lines[idxs[0]].match(/^\s*/)[0];
  let pos = idxs[0];
  for (const i of idxs) {
    if (lines[i].trim() > newLine) {
      pos = i;
      break;
    }
    pos = i + 1;
  }
  lines.splice(pos, 0, indent + newLine);
}

function escapeKotlinString(s) {
  // Escape for a Kotlin string literal.
  return s.replace(/\\/g, "\\\\").replace(/"/g, '\\"');
}

function main() {
  const args = process.argv.slice(2);
  const positional = [];
  let title = null;
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--title") {
      title = args[++i];
      if (title === undefined) fail("--title requires a value");
    } else if (args[i].startsWith("--title=")) {
      title = args[i].slice("--title=".length);
    } else if (args[i].startsWith("-")) {
      fail(`unknown option: ${args[i]}`);
    } else {
      positional.push(args[i]);
    }
  }
  if (positional.length !== 1) {
    console.error('usage: node scripts/new-feature.mjs <name> [--title "Display Title"]');
    process.exit(1);
  }
  const name = positional[0];
  if (!NAME_RE.test(name)) {
    fail("name must be lowercase letters/digits (e.g. 'arena'); no spaces, dashes or uppercase");
  }
  if (!existsSync(join(ROOT, "feature")) || !existsSync(join(ROOT, "settings.gradle.kts"))) {
    fail("run this script from the repository root (or keep it in scripts/)");
  }

  const module = join(ROOT, "feature", name);
  if (existsSync(module)) fail(`feature/${name} already exists`);

  const titleSafe = escapeKotlinString(title ?? name[0].toUpperCase() + name.slice(1));
  const Name = name[0].toUpperCase() + name.slice(1);

  // 1. Create the module skeleton.
  const pkgDir = join(module, "src", "commonMain", "kotlin", "com", "imnaiyar", "skytimes", name);
  mkdirSync(pkgDir, { recursive: true });
  writeFileSync(join(module, "build.gradle.kts"), FEATURE_BUILD_TEMPLATE);
  writeFileSync(join(pkgDir, `${Name}Screen.kt`), screenTemplate(name, Name, titleSafe));
  console.log(`created feature/${name}/`);

  // 2. settings.gradle.kts -- register the module.
  let p = join(ROOT, "settings.gradle.kts");
  let lines = read(p);
  sortedInsert(lines, /^\s*include\(":feature:[a-z0-9]+"\)$/, `include(":feature:${name}")`, "settings.gradle.kts");
  write(p, lines);
  console.log(`updated settings.gradle.kts (include :feature:${name})`);

  // 3. app/build.gradle.kts -- depend on the new feature.
  p = join(ROOT, "app", "build.gradle.kts");
  lines = read(p);
  sortedInsert(lines, /^\s*implementation\(projects\.feature\.[a-z0-9]+\)$/, `implementation(projects.feature.${name})`, "app/build.gradle.kts");
  write(p, lines);
  console.log(`updated app/build.gradle.kts (implementation projects.feature.${name})`);

  console.log();
  console.log(`Feature '${Name}' created and registered.`);
}

main();
