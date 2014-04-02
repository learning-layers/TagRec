TagRec
======

##Towards A Standardized Tag Recommender Benchmarking Framework

## Description
The aim of this work is to provide the community with a simple to use, generic tag-recommender framework written in Java to evaluate novel tag-recommender algorithms with a set of well-known std. IR metrics such as MAP, MRR, P@k, R@k, F1@k and folksonomy datasets such as BibSonomy, CiteULike, LastFM or Delicious and to benchmark the developed approaches against state-of-the-art tag-recommender algorithms such as MP, MP_r, MP_u, MP_u,r, CF, APR, FR, GIRP, GIRPTM, etc.

Furthermore, it contains algorithms to process datasets (e.g., p-core pruning, leave-one-out splitting and LDA topic creation).

The software already contains three novel tag-recommender approaches based on cognitive science theory. The first one ([3Layers](http://www.christophtrattner.info/pubs/cikm2013.pdf)) uses topic information and is based on the ALCOVE theory (Krutschke et al., 1992). The second one ([BLL+C](http://arxiv.org/pdf/1312.5111.pdf)) uses time information is based on the ACT-R theory (Anderson et al., 2004). The third one ([3LT](http://arxiv.org/pdf/1402.0728v1.pdf)) is a combination of the former two approaches and integrates the time component on the level of tags and topics.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

Please cite [the papers](https://github.com/domkowald/tagrecommender#references) if you use this software in one of your publications.

## Download
The source-code can be directly checked-out through this repository. It contains an Eclipse project to edit and build it and an already deployed .jar file for direct execution. Furthermore, the folder structure that is provided in the repository is needed, where _csv_ is the input directory and _metrics_ is the output directory in the _data_ folder. Both of these directories contain subdirectories for the different datasets:
* bib_core for BibSonomy
* cul_core for CiteULike
* flickr_core for Flickr
* wiki_core for Wikipedia (based on bookmarks from Delicious)

## How-to-use
The _tagrecommender_ .jar uses three parameters:
First the algorithm:
* 3layers for 3Layers (based on ALCOVE theory) (Seitlinger et al., 2013)
* 3LT for the time-based 3Layers on the levels of tags and topics (Kowald et al., 2014a)
* bll_c for BLL and BLL+C (based on ACT-R theory) (Kowald et al., 2014b)
* lda for Latent Dirichlet Allocation (Krestel et al., 2009)
* cf for Collaborative Filtering (Jäschke et al., 2007)
* fr for Adapted PageRank and FolkRank (Hotho et al., 2006)
* girptm for GIRP and GIRPTM (Zhang et al., 2012)
* mp for MostPopular tags
* mp_u_r for MostPopular tags by user and/or resource (Jäschke et al., 2007)
* core for calculating p-cores on a dataset
* split for splitting a dataset into training and test-sets using a leave-one-out method
* lda_samples for creating LDA topics for the resources in a dataset

, second the dataset(-directory):
* bib for BibSonomy
* cul for CiteULike
* flickr for Flickr
* wiki for Wikipedia (based on bookmarks from Delicious)

and third the filename (without file extension)

**Example:**
`java -jar tagrecommender.jar bll_c bib bib_sample`

## Input format
The input-files have to be placed in the corresponding subdirectory and are in csv-format (file extension: .txt) with 5 columns (quotation marks are mandatory):
* User
* Resource
* Timestamp in seconds
* List of tags
* List of categories (optional)

**Example:**
"0";"13830";"986470059";"deri,web2.0,tutorial,www,conference";""

There are three files needed:
* one file for training (with _train suffix)
* one file for testing (with _test suffix)
* one file that first contains the training-set and then the test-set (no suffix - is used for generating indices for the calculations)

**Example:**
bib_sample_train.txt, bib_sample_test.txt, bib_sample.txt (combination of train and test file)

## Output format
The output-file is generated in the corresponding subdirectory and is in csv-format with 5 columns:
* Recall
* Precision
* F1-score
* Mean Reciprocal Rank
* Mean Average Precision

for _k_ = 1 to 10 (each line is one _k_)

**Example:**
0,5212146123336273;0,16408544726301685;0,22663857529082376;0,26345775109372344;0,3242776089324113

## References
* D. Kowald, P. Seitinger, C. Trattner, and T. Ley.: [Forgetting the Words but Remembering the Meaning: Modeling Forgetting in a Verbal and Semantic Tag Recommender](http://arxiv.org/pdf/1402.0728v1.pdf), 2014a. (under review)
* D. Kowald, P. Seitlinger, C. Trattner, and T. Ley. [Long Time No See: The Probability of Reusing Tags as a Function of Frequency and Recency](http://www.sheridanprinting.com/14-www-comp2ch4rv3-19/companion/p463.pdf). In Proceedings of the 23rd international conference on World Wide Web, WWW '14, Seoul, Korea, 2014b. ACM.
* P. Seitinger, D. Kowald, C. Trattner, and T. Ley.: [Recommending Tags with a Model of Human Categorization](http://www.christophtrattner.info/pubs/cikm2013.pdf). In The ACM International Conference on Information and Knowledge Management (CIKM 2013), ACM, New York, NY, USA, 2013.

* A. Hotho, R. Jäschke, C. Schmitz, and G. Stumme. Information retrieval in folksonomies: Search and ranking. In The semantic web: research and applications, pages 411–426. Springer, 2006.
* L. Zhang, J. Tang, and M. Zhang. Integrating temporal usage pattern into personalized tag prediction. In Web Technologies and Applications, pages 354–365. Springer, 2012.
* R. Jäschke, L. Marinho, A. Hotho, L. Schmidt-Thieme, and G. Stumme. Tag recommendations in folksonomies. In Knowledge Discovery in Databases: PKDD 2007, pages 506–514. Springer, 2007.
* R. Krestel, P. Fankhauser, and W. Nejdl. Latent dirichlet allocation for tag recommendation. In Proceedings of the third ACM conference on Recommender systems, pages 61–68. ACM, 2009.
* J. R. Anderson, M. D. Byrne, S. Douglass, C. Lebiere, and Y. Qin. An integrated theory of the mind. Psychological Review, 111(4):1036–1050, 2004.
* J. K. Kruschke et al. Alcove: An exemplar-based connectionist model of category learning. Psychological review, 99(1):22–44, 1992.

## Contact
* Dominik Kowald, Know-Center, Graz University of Technology, dkowald@know-center.at
* Christoph Trattner, Know-Center, Graz University of Technology, ctrattner@know-center.at


