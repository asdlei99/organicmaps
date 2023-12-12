#include "drape/font.hpp"

#include "platform/platform.hpp"

#include "base/file_name_utils.hpp"
#include "base/scope_guard.hpp"
#include "base/string_utils.hpp"

#include <fstream>
#include <ft2build.h>
#include <iostream>
#include FT_FREETYPE_H

struct TextRun
{
  int32_t start, end;
  int font;
};


typedef buffer_vector<TextRun, 5> TextRuns;
struct FontParams {
  int pixelSize;
  int8_t lang;
};


TextRuns ItemizeAndShapeText(std::string_view utf8, int8_t lang, FontParams const & fontParams);

int main(int argc, char** argv)
{
  if (argc < 2)
  {
    std::cerr << "Usage: " << argv[0] << " <path to a directory with ttf files> [path to test text file]\n";
    return -1;
  }
  std::string const kFontsDir = argv[1];
  Platform::FilesList ttfFiles;
  Platform::GetFilesByExt(kFontsDir, ".ttf", ttfFiles);

  // Initialize Freetype.
  FT_Library library;
  if (auto const err = FT_Init_FreeType(&library); err != 0)
  {
    std::cerr << "FT_Init_FreeType returned " << err << " error\n";
    return 1;
  }
  SCOPE_GUARD(doneFreetype, [&library]()
              {
                if (auto const err = FT_Done_FreeType(library); err != 0)
                  std::cerr << "FT_Done_FreeType returned " << err << " error\n";
              });

  // Scan all fonts.
  // std::vector<dp::Font> fonts;
  // for (auto const & ttf : ttfFiles)
  // {
  //   std::cout << ttf << "\n";
  //   fonts.emplace_back(4, GetPlatform().GetReader(base::JoinPath(kFontsDir, ttf)), library);
  //   std::vector<FT_ULong> charcodes;
  //   fonts.back().GetCharcodes(charcodes);
  // }

/////////////////////////
  if (argc >= 3)
  {
    std::ifstream file(argv[2]);
    std::string line;
    while (file.good())
    {
      std::getline(file, line);
      strings::Trim(line);
      if (line.empty()) continue;

      auto const runs = ItemizeAndShapeText(line, 0, FontParams{});
      std::cout << line << "\n";
      size_t pos = 0;
      for (size_t i = 0; i < runs.size(); ++i)
      {
        auto const & run = runs[i];
        std::cout << run.start << "-" << run.end << " ";
        // while (pos++ < run.end)
        // {
        //   std::cout << i + 1;
        // }
      }
      std::cout << "\n";
    }
  }


  return 0;
}
